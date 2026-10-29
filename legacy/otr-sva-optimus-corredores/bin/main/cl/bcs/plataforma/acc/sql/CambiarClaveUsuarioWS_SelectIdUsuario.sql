SELECT
"IdUsuario"
FROM "BSCL.Plataforma.BD::ACC.Usuario" 
WHERE UPPER("CodigoUsuario")  = UPPER(:p_CodigoUsuario)
AND "IdEmpresa" = :p_IdEmpresa;
