SELECT
"IdTipoEvento"
,"Nombre"
,"Estado"
FROM "BSCL.Contabilidad.BD::ITZ.EventoTipo"
WHERE "IdTipoEvento" IN (:IdTipoEvento)