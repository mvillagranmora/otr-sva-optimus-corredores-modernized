SELECT
	'D' AS "TipoRegistro" --Constante
	,TO_CHAR(COMPROBANTE."FechaComprobante",'DDMMYYYY') AS "FechaOperacionPoliza"
	,COMPROBANTE."IdComprobante" AS "NumeroPolizaEmitida"
	,COALESCE(MOVIMIENTO."FolioTransaccion",'0') AS "FolioOperacion"
	,ABS(TO_DECIMAL(COALESCE(MOVIMIENTO."Monto",0),26,8)) AS "ImportePagadoCliente" 
	,TO_CHAR(COALESCE(MOVIMIENTO."FechaOrden",'0001-01-01'),'DDMMYYYY') AS "FechaOrden"
	,COALESCE("IdOrden",0) AS "FolioOrden"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COMPROBANTE
LEFT OUTER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MOVIMIENTO ON (MOVIMIENTO."IdComprobante" = COMPROBANTE."IdComprobante" AND MOVIMIENTO."IdEstadoFacturacion" IN ('FAC','ENV'))
WHERE (TO_CHAR(COMPROBANTE."FechaComprobante",'YYYY-MM-DD') = TO_CHAR(:FechaDesde,'YYYY-MM-DD'))
AND (:EsTodosCliente = 'S' OR COMPROBANTE."IdCliente" = :IdCliente)
AND (:EsTodosInstrumentos = 'S' OR MOVIMIENTO."IdInstrumento" = :IdInstrumento)
AND COMPROBANTE."IdEmpresa" = :IdEmpresa;