-- Inserta en tabla global datos de Comprobante Detalle Origen
INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalleOrigen" 
("IdComprobanteDetalle","IdIngresoEgreso","IdComprobante")
SELECT CD."IdComprobanteDetalle", MI."IdIngresoEgreso", CAST(null AS BIGINT) AS "IdComprobante"
  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteDetalle" as CD
 INNER JOIN "BSCL.Tesoreria.BD::GBL.MovimientoDetalleGenerarComprobante" as MI 
 		 ON MI."IdComprobante" = CD."IdComprobante"
 		AND MI."IdTipoMovimientoDetalle" = CD."IdTipoMovimiento"
 		AND MI."IdConcepto" = CD."IdConcepto"
		AND COALESCE(MI."SecuenciaAgrupacion",'') = COALESCE(CD."SecuenciaAgrupacion",'')
 		AND COALESCE(MI."SecuenciaRelacionada",0) = COALESCE(CD."SecuenciaRelacionada",0);