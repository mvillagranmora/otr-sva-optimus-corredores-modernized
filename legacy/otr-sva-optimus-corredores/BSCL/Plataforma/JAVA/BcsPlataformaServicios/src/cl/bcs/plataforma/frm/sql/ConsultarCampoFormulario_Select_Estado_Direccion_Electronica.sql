SELECT
"IdEstado", 
"Nombre"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarEstadoDireccionElectronica"
WHERE "IdEstado" in (:IdsEstado);