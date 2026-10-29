SELECT
"IdClasificacionFatca", 
"Nombre"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionFatca"
WHERE "IdClasificacionFatca" in (:IdsClasificacionFatca);