SELECT
"IdEstado", 
"Nombre"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoNombreFantasia"
WHERE "IdEstado" in (:IdsEstadoNombreFantasia);