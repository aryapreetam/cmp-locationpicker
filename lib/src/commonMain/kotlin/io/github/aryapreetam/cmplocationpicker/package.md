# Package io.github.aryapreetam.cmplocationpicker

Core types and entry points for `cmp-locationpicker`.

Most apps will use:

- `io.github.aryapreetam.cmplocationpicker.ui.LocationPickerDialog`

v0 returns coordinates only:

- `io.github.aryapreetam.cmplocationpicker.model.LatLng`

Networking and privacy-sensitive behavior (best-effort IP-based initial center) is isolated behind:

- `io.github.aryapreetam.cmplocationpicker.provider.IpLocationProvider`
