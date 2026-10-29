SELECT
"Codigo", 
"Descripcion"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarArea"
WHERE "Codigo" in (:IdsCodigo);