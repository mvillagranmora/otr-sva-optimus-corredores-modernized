SELECT 
	COM."IdComprobante" AS "IdComprobante"
	,INGEGR."IdIngresoEgreso" AS "IdIngresoEgreso"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteDetalle" COM
INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" INGEGR
	ON COM."IdComprobanteOrigen" = INGEGR."IdComprobanteOrigen"
WHERE COM."IdComprobanteOrigen" = :p_ComprobanteOrigen
ORDER BY COM."IdComprobante" DESC, INGEGR."IdIngresoEgreso" DESC;