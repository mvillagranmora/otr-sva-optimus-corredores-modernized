SELECT
"IdEstado", 
"Nombre"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoTelefono"
WHERE "IdEstado" in (:IdsEstado);