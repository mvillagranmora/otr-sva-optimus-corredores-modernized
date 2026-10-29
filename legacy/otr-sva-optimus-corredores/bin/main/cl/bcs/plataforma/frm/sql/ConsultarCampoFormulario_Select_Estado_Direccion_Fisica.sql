SELECT
"IdEstado", 
"Nombre"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDireccionFisica"
WHERE "IdEstado" in (:IdsEstado);