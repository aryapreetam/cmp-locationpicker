# `io.github.aryapreetam.cmplocationpicker`

This is the root package for `cmp-locationpicker`.

Most apps will use:

- `io.github.aryapreetam.cmplocationpicker.ui.LocationPickerDialog`

Key packages:

- `model`: data types like `LatLng`
- `ui`: the public Compose dialog + configuration (`LocationPickerStrings`, `LocationPickerOptions`)
- `provider`: extension points (`IpLocationProvider`, `MapProvider`)
- `core`/`protocol`: internal core + JS↔Compose protocol

For installation and end-to-end examples, see the repo `README.MD`.
