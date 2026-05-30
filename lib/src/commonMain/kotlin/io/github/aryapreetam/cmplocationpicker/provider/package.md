# Package io.github.aryapreetam.cmplocationpicker.provider

Provider abstractions and extension points.

Key interfaces:

- `IpLocationProvider`: best-effort IP-based initial center lookup. Provide your own implementation to disable IP lookup or to use a different service.
- `MapProvider`: generates controlled map `htmlContent` and defines the JS↔Compose contract.

v0 defaults:

- `DefaultIpLocationProvider` (endpoint: `https://ipapi.co/json/`)
- `io.github.aryapreetam.cmplocationpicker.provider.osm.LeafletOsmMapProvider` (Leaflet + OpenStreetMap + Nominatim)
