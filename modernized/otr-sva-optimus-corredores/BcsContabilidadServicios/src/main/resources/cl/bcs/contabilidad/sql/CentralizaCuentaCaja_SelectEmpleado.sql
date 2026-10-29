SELECT
	E."IdEmpleado" AS "IdEmpleado"  
FROM "BSCL.SocioNegocio.BD::EMP.Empleado" AS E
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS U ON U."IdEntidad" = E."IdEmpleado"
WHERE
	U."TipoEntidad" = 'EMP'
AND "IdUsuario" = :IdUsuario;	