SELECT
EV."IdPreComprobanteAsiento"
,EV."IdEvento"
,EV."Operador"
,EV."Linea"
,EV."Estado"
,EV."GlosaDetalle"
FROM "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" EV
INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" PA ON (EV."IdPreComprobanteAsiento" = PA."IdPreComprobanteAsiento")
WHERE PA."IdPreComprobante" IN (:IdPreComprobanteAnt)