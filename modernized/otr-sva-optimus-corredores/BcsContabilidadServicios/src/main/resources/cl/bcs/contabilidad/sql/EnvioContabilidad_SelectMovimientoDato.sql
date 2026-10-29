SELECT 
CTB_itzMovimientoDato."IdMovimiento"
,CTB_itzMovimientoDato."IdTipoDatoEvento"
,CTB_itzMovimientoDato."ValorVarchar"
,CTB_itzMovimientoDato."ValorDecimal"
,CTB_itzMovimientoDato."Estado"
,CTB_itzMovimientoDato."TipoEvento"
,ETD."TipoDato"
FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzprecomprobante
INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento ON CTB_itzprecomprobante."IdPreComprobante" = CTB_itzPreComprobanteAsiento."IdPreComprobante"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento ON CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento ON CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento ON CTB_itzMovimiento."IdMovimiento" = CTB_itzEvento."IdMovimiento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato ON CTB_itzMovimiento."IdMovimiento" = CTB_itzMovimientoDato."IdMovimiento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" AS ETD ON (ETD."IdTipoDatoEvento" = CTB_itzMovimientoDato."IdTipoDatoEvento")
WHERE CTB_itzprecomprobante."Estado" IN ('V', 'D')
AND CTB_itzprecomprobante."IdPreComprobante" IN (:IdPrecomprobante) AND NOT EXISTS (
SELECT 1 FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato" MD WHERE MD."IdMovimiento" = CTB_itzMovimientoDato."IdMovimiento" AND MD."TipoEvento" <> '' AND MD."TipoEvento" = CTB_itzEvento."IdTipoEvento"  
)
UNION ALL
SELECT 
CTB_itzMovimientoDato."IdMovimiento"
,CTB_itzMovimientoDato."IdTipoDatoEvento"
,CTB_itzMovimientoDato."ValorVarchar"
,CTB_itzMovimientoDato."ValorDecimal"
,CTB_itzMovimientoDato."Estado"
,CTB_itzMovimientoDato."TipoEvento"
,ETD."TipoDato"
FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" AS CTB_itzprecomprobante
INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" AS CTB_itzPreComprobanteAsiento ON CTB_itzprecomprobante."IdPreComprobante" = CTB_itzPreComprobanteAsiento."IdPreComprobante"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" AS CTB_itzEventoPreComprobanteAsiento ON CTB_itzPreComprobanteAsiento."IdPreComprobanteAsiento" = CTB_itzEventoPreComprobanteAsiento."IdPreComprobanteAsiento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Evento" AS CTB_itzEvento ON CTB_itzEventoPreComprobanteAsiento."IdEvento" = CTB_itzEvento."IdEvento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.Movimiento" AS CTB_itzMovimiento ON CTB_itzMovimiento."IdMovimiento" = CTB_itzEvento."IdMovimiento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.MovimientoDato" AS CTB_itzMovimientoDato ON CTB_itzMovimiento."IdMovimiento" = CTB_itzMovimientoDato."IdMovimiento" AND CTB_itzMovimientoDato."TipoEvento" = CTB_itzEvento."IdTipoEvento"
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EventoTipoDato" AS ETD ON (ETD."IdTipoDatoEvento" = CTB_itzMovimientoDato."IdTipoDatoEvento")
WHERE CTB_itzprecomprobante."Estado" IN ('V', 'D')
AND CTB_itzprecomprobante."IdPreComprobante" IN (:IdPrecomprobante)