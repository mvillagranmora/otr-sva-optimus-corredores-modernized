SELECT
    ORD."Cantidad" AS "Cantidad",
    ASIG."CodigoIFRS" AS "CodigoIFRS",
    "IndicadorFacturacionInmediata" AS "EsFacturaInmediata",
    TO_VARCHAR("FechaRecepcion", 'YYYY-MM-DD') AS "FechaRecepcion",
    TRA_NUEVO."FolioCartera" AS "FolioCartera",
    TRA_NUEVO."Folio" AS "FolioTransaccion",
    ORD."Glosa" AS "Glosa",
    ORD."IdBoveda" AS "IdBoveda",
    ORD."IdCuentaCustodioPortafolio" AS "IdCuentaCustodioPortafolio",
    ORD."IdFormaCustodia" AS "IdFormaCustodia",
    COALESCE(ASIG."IdAsignacion",TO_BIGINT(0)) AS "IdInterno",
    ORD."IdMonedaLiquidacionOrden" AS "IdMonedaLiquidacionOrden",
    ORD."IdPortafolioCliente" AS "IdPortafolio",
    TRA_NUEVO."IdTransaccion" AS "IdTransaccion",
    'COR' AS "IngresoDesde",
    CASE  FPO."IdMedioDePago" 
    	WHEN 'CIN' THEN 'S'
    	ELSE 'N'
    END AS "PagoCobroCuentaInversion",
    FPO."IdPreferenciaPagoPortafolio",
    ORD."PlazoVentaCorta" AS "PlazoVentaCorta",
    ORD."TasaVentaCorta" AS "TasaVentaCorta",
    ORD."TipoCambio" AS "TipoCambio",
    ORD."ValorRescate" AS "ValorRescate",
    MC."IdEstadoFacturacion" AS "IdEstadoFacturacion",
    ASIG."IdEstadoAsignacion" AS "IdEstadoAsignacion"
FROM "BSCL.Orden.BD::ORD.Transaccion" TRA
INNER JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA_NUEVO ON TRA."Folio"=TRA_NUEVO."Folio" AND TRA."FechaSistemaCreacion"=TRA_NUEVO."FechaSistemaCreacion" AND TRA_NUEVO."IdEstado"='ING' AND TRA."IdTipoOperacion"=TRA_NUEVO."IdTipoOperacion"
LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ASIG ON TRA."IdTransaccion" = ASIG."IdTransaccion"
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORD ON ORD."IdOrden" = ASIG."IdOrden"
LEFT JOIN "BSCL.Orden.BD::ORD.FormaDePagoOrden" FPO ON ORD."IdOrden"=FPO."IdOrden" 
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MC ON MC."IdAsignacion" = ASIG."IdAsignacion"
WHERE TRA."IdTransaccion" IN (:TransaccionesAReasignar) AND ASIG."IdEstadoAsignacion" = 'ANU' ORDER BY ASIG."IdAsignacion"