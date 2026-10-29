SELECT
EVT."IdTipoEvento"
,EVT."IdDefinicionAsiento"
,EVT."Operador"
,EVT."Estado"
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" EVT
INNER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" DEF ON (EVT."IdDefinicionAsiento" = DEF."IdDefinicionAsiento")
WHERE DEF."IdTipoContabilidad" IN (:IdTipoContabilidad)