package io.github.aryapreetam.cmplocationpicker.provider.osm

import io.github.aryapreetam.cmplocationpicker.model.LatLng
import io.github.aryapreetam.cmplocationpicker.provider.MapProvider
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerOptions
import io.github.aryapreetam.cmplocationpicker.ui.LocationPickerStrings

/** Default v0 provider: Leaflet + OSM tiles + Nominatim search. */
class LeafletOsmMapProvider : MapProvider {
  override fun createHtml(
    fallbackCenter: LatLng,
    strings: LocationPickerStrings,
    options: LocationPickerOptions
  ): String {
    val searchPlaceholderHtml = strings.searchPlaceholder.htmlAttrEscape()
    val noResultsJs = strings.noResults.jsQuoted()
    val searchErrorJs = strings.searchError.jsQuoted()
    val limit = options.suggestionsLimit.coerceIn(1, 20)
    val countryCodesJs = options.countryCodes
      ?.takeIf { it.isNotBlank() }
      ?.trim()
      ?.jsQuoted()
    val languageTagJs = options.searchLanguageTag
      ?.takeIf { it.isNotBlank() }
      ?.trim()
      ?.jsQuoted()

    val countryCodesParamLine = countryCodesJs
      ?.let { "url.searchParams.set('countrycodes', $it);" }
      .orEmpty()
    val languageTagParamLine = languageTagJs
      ?.let { "url.searchParams.set('accept-language', $it);" }
      .orEmpty()

    val centerLat = fallbackCenter.latitude
    val centerLng = fallbackCenter.longitude

    // language=HTML
    return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>Location Picker</title>
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
              integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
        <style>
          body { margin: 0; padding: 0; font-family: Arial, sans-serif; }
          #map { width: 100vw; height: 100vh; }

          /* Center marker overlay: selection is always the map center. */
          .center-marker {
            position: fixed;
            top: 50%;
            left: 50%;
            width: 25px;
            height: 41px;
            margin-left: -12px;
            margin-top: -41px;
            z-index: 1000;
            pointer-events: none;
            background-image: url('https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png');
            background-size: contain;
            background-repeat: no-repeat;
          }
          .center-marker::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 25px;
            height: 10px;
            background-image: url('https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png');
            background-size: contain;
            background-repeat: no-repeat;
          }

          .search-container {
            position: absolute;
            top: 10px;
            left: 50px;
            right: 10px;
            z-index: 1000;
            background: white;
            padding: 10px;
            border-radius: 5px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
            max-width: 400px;
          }

          .search-input {
            width: 100%;
            padding: 8px;
            padding-right: 38px;
            border: 1px solid #ccc;
            border-radius: 4px;
            font-size: 14px;
            box-sizing: border-box;
          }

          .clear-search {
            position: absolute;
            top: 10px;
            right: 10px;
            width: 34px;
            height: 34px;
            border: none;
            border-radius: 17px;
            background: transparent;
            color: #666;
            font-size: 20px;
            line-height: 34px;
            cursor: pointer;
            display: none;
          }
          .clear-search:hover { background: rgba(0,0,0,0.06); }

          .suggestions {
            margin-top: 5px;
            max-height: 200px;
            overflow-y: auto;
            border: 1px solid #ccc;
            border-radius: 4px;
            display: none;
            background: white;
          }

          .suggestion-item {
            padding: 8px;
            cursor: pointer;
            border-bottom: 1px solid #eee;
            font-size: 13px;
          }

          .suggestion-item:hover {
            background: #f0f0f0;
          }

          .suggestion-item:last-child {
            border-bottom: none;
          }

          .loading {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            z-index: 2000;
            background: rgba(255, 255, 255, 0.9);
            padding: 20px;
            border-radius: 5px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
          }

          .coords-display {
            position: absolute;
            bottom: 10px;
            left: 10px;
            z-index: 1000;
            background: rgba(255, 255, 255, 0.9);
            padding: 8px;
            border-radius: 4px;
            font-size: 12px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
            max-width: 300px;
          }
        </style>
      </head>
      <body>
        <div id="loading" class="loading">Loading map...</div>
        <div id="map"></div>

        <div class="center-marker"></div>

        <div class="search-container">
          <input
            type="text"
            id="searchInput"
            class="search-input"
            placeholder="$searchPlaceholderHtml"
          />
          <button id="clearSearch" class="clear-search" type="button" aria-label="Clear">×</button>
          <div id="suggestions" class="suggestions"></div>
        </div>

        <div id="coordsDisplay" class="coords-display">Loading location…</div>

        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
                integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>

        <script>
          (function() {
            let bridgeReady = false;
            const pendingPosts = [];

            function canPost() {
              return window.ComposeWebViewBridge && typeof window.ComposeWebViewBridge.postMessage === 'function';
            }

            function flushPending() {
              if (!canPost()) return;
              bridgeReady = true;
              while (pendingPosts.length > 0) {
                try {
                  const obj = pendingPosts.shift();
                  window.ComposeWebViewBridge.postMessage(JSON.stringify(obj));
                } catch (e) {
                  // Ignore.
                }
              }
            }

            function postToCompose(obj) {
              try {
                if (!bridgeReady) flushPending();
                if (!bridgeReady) {
                  pendingPosts.push(obj);
                  return;
                }
                window.ComposeWebViewBridge.postMessage(JSON.stringify(obj));
              } catch (e) {
                // Ignore.
              }
            }

            // Ensure we flush once the WebView bridge is ready.
            window.addEventListener('ComposeWebViewBridgeReady', flushPending);
            document.addEventListener('ComposeWebViewBridgeReady', flushPending);

            let msgCounter = 0;
            let lastPendingId = null;

            function nextId() {
              msgCounter += 1;
              return String(Date.now()) + '-' + String(msgCounter);
            }

            function sendLocationUpdate(lat, lng) {
              // Ack-based: do not pile up multiple pending messages.
              if (lastPendingId !== null) return;
              const id = nextId();
              lastPendingId = id;
              postToCompose({ type: 'location_update', id: id, lat: lat, lng: lng });
            }

            window.CmpLocationPickerBridge = {
              onAck: function(ack) {
                try {
                  if (ack && ack.type === 'ack' && ack.id === lastPendingId) {
                    lastPendingId = null;
                  }
                } catch (e) {
                  // Ignore.
                }
              },
              setCenter: function(lat, lng) {
                try {
                  if (window.__cmp_locationpicker_map) {
                    window.__cmp_locationpicker_map.setView([lat, lng], Math.max(window.__cmp_locationpicker_map.getZoom(), 12));
                    updateFromCenter();
                  }
                } catch (e) {
                  // Ignore.
                }
              }
            };

            function updateCoords(lat, lng) {
              const coordsDisplay = document.getElementById('coordsDisplay');
              if (!coordsDisplay) return;
              coordsDisplay.textContent = 'Lat: ' + lat.toFixed(6) + ', Lng: ' + lng.toFixed(6);
            }

            // Initialize map
            const map = L.map('map', {
              zoomControl: true,
              attributionControl: false,
              maxBounds: [[6.7471, 68.1766], [35.6745, 97.4026]],
              maxBoundsViscosity: 1.0,
              minZoom: 4,
              maxZoom: 18,
              zoomSnap: 0.5,
              zoomDelta: 0.5,
              wheelDebounceTime: 150
            }).setView([$centerLat, $centerLng], 5);
            window.__cmp_locationpicker_map = map;

            L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
              maxZoom: 19,
              attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/">CARTO</a>',
              subdomains: 'abcd'
            }).addTo(map);

            function updateFromCenter() {
              const center = map.getCenter();
              updateCoords(center.lat, center.lng);
              sendLocationUpdate(center.lat, center.lng);
            }

            map.on('click', function(e) {
              map.panTo(e.latlng);
            });

            let moveTimeout;
            map.on('moveend zoomend', function() {
              clearTimeout(moveTimeout);
              moveTimeout = setTimeout(function() {
                updateFromCenter();
              }, 200);
            });

            const searchInput = document.getElementById('searchInput');
            const suggestionsDiv = document.getElementById('suggestions');
            const clearSearchBtn = document.getElementById('clearSearch');

            function setClearVisible(visible) {
              clearSearchBtn.style.display = visible ? 'block' : 'none';
            }

            let searchTimeout;
            searchInput.addEventListener('input', function() {
              clearTimeout(searchTimeout);
              searchTimeout = setTimeout(function() {
                const query = searchInput.value.trim();
                setClearVisible(query.length > 0);
                if (query.length < 3) {
                  suggestionsDiv.style.display = 'none';
                  return;
                }
                searchPlaces(query);
              }, 300);
            });

            clearSearchBtn.addEventListener('click', function() {
              searchInput.value = '';
              setClearVisible(false);
              suggestionsDiv.style.display = 'none';
              searchInput.focus();
            });

            async function searchPlaces(query) {
              try {
                const url = new URL('https://nominatim.openstreetmap.org/search');
                url.searchParams.set('format', 'json');
                url.searchParams.set('q', query);
                url.searchParams.set('limit', String($limit));
                url.searchParams.set('addressdetails', '1');
                $countryCodesParamLine
                $languageTagParamLine

                const response = await fetch(url.toString());
                const results = await response.json();
                showSuggestions(results);
              } catch (error) {
                showError();
              }
            }

            function showError() {
              suggestionsDiv.innerHTML = '';
              const div = document.createElement('div');
              div.className = 'suggestion-item';
              div.textContent = $searchErrorJs;
              suggestionsDiv.appendChild(div);
              suggestionsDiv.style.display = 'block';
            }

            function showSuggestions(results) {
              suggestionsDiv.innerHTML = '';
              if (!results || results.length === 0) {
                const div = document.createElement('div');
                div.className = 'suggestion-item';
                div.textContent = $noResultsJs;
                suggestionsDiv.appendChild(div);
                suggestionsDiv.style.display = 'block';
                return;
              }

              results.forEach(function(result) {
                const div = document.createElement('div');
                div.className = 'suggestion-item';
                div.textContent = result.display_name;
                div.addEventListener('click', function() {
                  const lat = parseFloat(result.lat);
                  const lon = parseFloat(result.lon);
                  if (!isNaN(lat) && !isNaN(lon)) {
                    const bbox = result.boundingbox;
                    try {
                      if (bbox && bbox.length === 4) {
                        // Nominatim: [south, north, west, east]
                        const south = parseFloat(bbox[0]);
                        const north = parseFloat(bbox[1]);
                        const west = parseFloat(bbox[2]);
                        const east = parseFloat(bbox[3]);

                        const latDiff = Math.abs(south - north);
                        const lngDiff = Math.abs(west - east);

                        if (latDiff < 0.001 || lngDiff < 0.001) {
                          map.setView([lat, lon], 17);
                        } else {
                          map.fitBounds([[south, west], [north, east]], { padding: [50, 50], maxZoom: 17 });
                        }
                      } else {
                        map.setView([lat, lon], 17);
                      }
                    } catch (e) {
                      map.setView([lat, lon], 17);
                    }
                  }
                  suggestionsDiv.style.display = 'none';
                  searchInput.value = '';
                  setClearVisible(false);
                  // Update selection after the map finishes moving.
                  setTimeout(function() { updateFromCenter(); }, 0);
                });
                suggestionsDiv.appendChild(div);
              });
              suggestionsDiv.style.display = 'block';
            }

            document.addEventListener('click', function(e) {
              if (!e.target.closest('.search-container')) {
                suggestionsDiv.style.display = 'none';
              }
            });

            map.whenReady(function() {
              // Initial state
              updateCoords($centerLat, $centerLng);
              sendLocationUpdate($centerLat, $centerLng);

              // Notify ready (after bridge becomes available).
              postToCompose({ type: 'ready', id: nextId() });

              // Hide loading
              const loading = document.getElementById('loading');
              if (loading) loading.style.display = 'none';
            });
          })();
        </script>
      </body>
      </html>
    """.trimIndent()
  }

  override fun setCenterJavaScript(latLng: LatLng): String {
    return "window.CmpLocationPickerBridge && window.CmpLocationPickerBridge.setCenter(${latLng.latitude}, ${latLng.longitude});"
  }
}

private fun String.jsString(): String {
  // Escape for embedding inside a JS double-quoted string literal.
  return buildString(length) {
    for (c in this@jsString) {
      when (c) {
        '\\' -> append("\\\\")
        '"' -> append("\\\"")
        '\n' -> append("\\n")
        '\r' -> append("\\r")
        '\t' -> append("\\t")
        else -> append(c)
      }
    }
  }
}

private fun String.jsQuoted(): String = "\"${this.jsString()}\""

private fun String.htmlAttrEscape(): String {
  return buildString(length) {
    for (c in this@htmlAttrEscape) {
      when (c) {
        '&' -> append("&amp;")
        '<' -> append("&lt;")
        '>' -> append("&gt;")
        '"' -> append("&quot;")
        else -> append(c)
      }
    }
  }
}
