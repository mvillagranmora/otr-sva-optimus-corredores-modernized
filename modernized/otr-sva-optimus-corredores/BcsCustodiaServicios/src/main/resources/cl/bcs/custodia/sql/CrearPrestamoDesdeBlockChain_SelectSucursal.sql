SELECT "IdSucursal"
FROM "BSCL.Plataforma.BD::ACC.Usuario" USU
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP ON EMP."IdEmpleado" = USU."IdEntidad" AND EMP."IdEmpresa" = USU."IdEmpresa" 
WHERE USU."TipoEntidad" = 'EMP'
		AND USU."IdEmpresa" = :p_IdEmpresa
		AND	"IdUsuario" = :p_IdUsuario
