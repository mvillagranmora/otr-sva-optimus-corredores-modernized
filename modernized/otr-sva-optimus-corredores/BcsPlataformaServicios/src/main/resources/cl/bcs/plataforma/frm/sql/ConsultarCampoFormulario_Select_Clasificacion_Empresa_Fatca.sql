SELECT
"IdClasificacionEmpresaFatca"
,"Nombre"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionEmpresaFatca"
WHERE "IdClasificacionEmpresaFatca" in (:IdsClasificacionEmpresaFatca);