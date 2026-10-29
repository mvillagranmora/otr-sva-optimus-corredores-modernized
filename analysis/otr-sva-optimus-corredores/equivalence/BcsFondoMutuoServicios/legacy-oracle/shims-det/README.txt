F3 W1 front 4 (ConsultarDetalleSolicitud): compile-only shims of record_det.sh.
None is needed so far: ConsultarDetalleSolicitud, its delegate ConsultarSolicitudAporteRescateUI and CalcularMontoNumeroCuota import nothing outside the closure that
record_det.sh compiles. The directory exists because record_det.sh scans it (find shims-det -name '*.java').
