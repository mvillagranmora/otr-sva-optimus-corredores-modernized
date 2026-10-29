WITH Transacciones AS (
    SELECT
        TRA."Folio" || TRA."FechaSistemaCreacion" || SUBSTR(TRA."IdTipoOperacion", 1, 3)     AS "KeyTransaccion",
        ORD."Cantidad"                            AS "Cantidad",
        ASIG."CodigoIFRS"                         AS "CodigoIFRS",
        ORD."IndicadorFacturacionInmediata"       AS "EsFacturaInmediata",
        TO_VARCHAR(ORD."FechaRecepcion", 'YYYY-MM-DD') AS "FechaRecepcion",
        TRA."FolioCartera"                        AS "FolioCartera",
        TRA."Folio"                               AS "FolioTransaccion",
        ORD."Glosa"                               AS "Glosa",
        ORD."IdBoveda"                            AS "IdBoveda",
        ORD."IdCuentaCustodioPortafolio"          AS "IdCuentaCustodioPortafolio",
        ORD."IdFormaCustodia"                     AS "IdFormaCustodia",
        CAST(0 AS BIGINT)                         AS "IdInterno",
        ORD."IdMonedaLiquidacionOrden"            AS "IdMonedaLiquidacionOrden",
        ORD."IdPortafolioCliente"                 AS "IdPortafolio",
        TRA."IdTransaccion"                       AS "IdTransaccion",
        'COR'                                     AS "IngresoDesde",
        CASE FPO."IdMedioDePago"
            WHEN 'CIN' THEN 'S'
            ELSE 'N'
        END                                       AS "PagoCobroCuentaInversion",
        FPO."IdPreferenciaPagoPortafolio",
        ORD."PlazoVentaCorta"                     AS "PlazoVentaCorta",
        ORD."TasaVentaCorta"                      AS "TasaVentaCorta",
        ORD."TipoCambio"                          AS "TipoCambio",
        ORD."ValorRescate"                        AS "ValorRescate",
        MC."IdEstadoFacturacion"                  AS "IdEstadoFacturacion",
        TRA."IdTipoOperacion"
    FROM "BSCL.Orden.BD::ORD.Transaccion" TRA
	LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ASIG
		ON TRA."IdTransaccion" = ASIG."IdTransaccion"
	LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORD
		ON ORD."IdOrden" = ASIG."IdOrden"
	LEFT JOIN "BSCL.Orden.BD::ORD.FormaDePagoOrden" FPO
		ON ORD."IdOrden" = FPO."IdOrden"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MC
		ON MC."IdAsignacion" = ASIG."IdAsignacion"
    WHERE
        TRA."Folio" || TRA."FechaSistemaCreacion" IN (:Llave)
        AND ASIG."IdEstadoAsignacion" = 'ANU'
)
SELECT *
FROM (
    SELECT
        ROW_NUMBER() OVER (
            PARTITION BY "KeyTransaccion"
            ORDER BY "IdTransaccion" DESC
        ) AS "Row",
        T.*
    FROM Transacciones T
)
WHERE "Row" = 1;
