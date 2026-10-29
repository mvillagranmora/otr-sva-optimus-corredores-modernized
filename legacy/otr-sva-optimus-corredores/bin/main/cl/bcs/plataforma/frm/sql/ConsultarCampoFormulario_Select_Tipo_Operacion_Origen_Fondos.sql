SELECT
"IdTipoOperacionOrigenFondos"
,"Nombre"
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOperacionOrigenFondos"
WHERE 
"IdEmpresa" = :IdsEmpresa AND 
"IdTipoOperacionOrigenFondos" in (:IdsTipoOperacionOrigenFondos);