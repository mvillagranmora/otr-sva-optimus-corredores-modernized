--Obtener comprobante de reportes (SOLO PERU)
WITH t_transacciones AS (
    SELECT
        C2."FolioTransaccion",
        C1."IdTipoOperacion"
    FROM
        "BSCL.Facturacion.BD::FAC.MovimientoComprobante" C1
        INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" C2 ON C1."IdMovimientoRelacionado" = C2."IdMovimientoComprobante"
    WHERE
        C1."IdComprobante" IN (:idsComprobante)
)
SELECT
    C."IdComprobante",
    C."EsVentaCorta",
    C."IdTipoComprobante",
    C."IdEstadoDTE",
    C."IdEstadoSII",
    C."IdEstadoComprobante",
    C."IdFormaCustodia",
    C."IdTipoOperacion",
    COALESCE("ValorTipoCambio", 0) AS "ValorTipoCambio",
    C."IdCliente",
    C."NombreLargoCliente",
    C."TipoDocumentoIdentidad",
    C."DocumentoIdentidad",
    C."IdPortafolio",
    C."IdTipoOperacion",
    C."IdTipoProducto",
    C."IdTipoCanalIngreso",
    C."Cantidad",
    C."Precio",
    C."MontoLiquidacionFinal",
    C."IdMoneda",
    C."IdMonedaLiquidacion",
    C."NemotecnicoInstrumento",
    C."IdTipoInstrumento",
    C."FechaLiquidacion",
    CP."Contrapartida",
    C."FechaComprobante",
    C."FechaComprobanteReferencia",
    C."IdComprobanteReferencia"
FROM
    "BSCL.Facturacion.BD::FAC.Comprobante" AS C
    LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS CP ON CP."IdConcepto" = C."IdConcepto"
WHERE
    C."IdComprobante" IN (
        --obtener todos los comprobantes que coincidan con los folios de las transacciones.
        SELECT
            "IdComprobante"
        FROM
            "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MC
            INNER JOIN t_transacciones T ON MC."FolioTransaccion" = T."FolioTransaccion"
            AND MC."IdTipoOperacion" = T."IdTipoOperacion"
    )
    AND "IdTipoInstrumento" IN ('RRV', 'RRF')
    AND "IdEstadoComprobante" <> 'ANU'
UNION
ALL --obtener comprobante (CHILE Y PERU).
SELECT
    C."IdComprobante",
    C."EsVentaCorta",
    C."IdTipoComprobante",
    C."IdEstadoDTE",
    C."IdEstadoSII",
    "IdEstadoComprobante",
    C."IdFormaCustodia",
    C."IdTipoOperacion",
    COALESCE("ValorTipoCambio", 0) AS "ValorTipoCambio",
    C."IdCliente",
    C."NombreLargoCliente",
    C."TipoDocumentoIdentidad",
    C."DocumentoIdentidad",
    C."IdPortafolio",
    C."IdTipoOperacion",
    C."IdTipoProducto",
    C."IdTipoCanalIngreso",
    C."Cantidad",
    C."Precio",
    C."MontoLiquidacionFinal",
    C."IdMoneda",
    C."IdMonedaLiquidacion",
    IFNULL("NemotecnicoInstrumento", '') AS "NemotecnicoInstrumento",
    IFNULL("IdTipoInstrumento", '') AS "IdTipoInstrumento",
    C."FechaLiquidacion",
    CP."Contrapartida",
    C."FechaComprobante",
    C."FechaComprobanteReferencia",
    C."IdComprobanteReferencia"
FROM
    "BSCL.Facturacion.BD::FAC.Comprobante" as C
    LEFT JOIN "BSCL.Facturacion.BD::FAC.ConceptoFacturacion" AS CP ON CP."IdConcepto" = C."IdConcepto"
WHERE
    C."IdComprobante" IN (:idsComprobante)