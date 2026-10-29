SELECT "CodigoUsuario"
FROM "BSCL.Plataforma.BD::ACC.Usuario"
WHERE "IdUsuario" = :IdUsuario
	AND "IdEmpresa" = :IdEmpresa;