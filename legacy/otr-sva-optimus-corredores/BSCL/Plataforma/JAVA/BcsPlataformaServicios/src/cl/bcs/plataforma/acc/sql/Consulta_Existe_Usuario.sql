SELECT 
"IdUsuario",
"IdEmpresa", "CodigoUsuario"
FROM "BSCL.Plataforma.BD::ACC.Usuario" 
WHERE "IdEmpresa" = :IdEmpresa 
AND "CodigoUsuario" = :CodigoUsuario;