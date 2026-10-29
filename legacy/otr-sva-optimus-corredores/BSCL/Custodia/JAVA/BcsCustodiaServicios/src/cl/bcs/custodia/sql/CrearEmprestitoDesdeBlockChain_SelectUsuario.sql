SELECT "IdEntidad"
FROM "BSCL.Plataforma.BD::ACC.Usuario"
WHERE "TipoEntidad" = 'EMP'
		AND "IdEmpresa" = :p_IdEmpresa
		AND	"IdUsuario" = :p_IdUsuario
