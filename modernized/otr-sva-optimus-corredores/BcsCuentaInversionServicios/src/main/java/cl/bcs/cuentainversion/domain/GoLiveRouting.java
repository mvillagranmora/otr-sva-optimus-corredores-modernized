package cl.bcs.cuentainversion.domain;

// B-CTA-07: routing decision for ConsultarMovimientoCuentaInversion Go-Live date boundary
public enum GoLiveRouting {
    HISTORICAL_ONLY,
    HYBRID,
    CURRENT_ONLY
}
