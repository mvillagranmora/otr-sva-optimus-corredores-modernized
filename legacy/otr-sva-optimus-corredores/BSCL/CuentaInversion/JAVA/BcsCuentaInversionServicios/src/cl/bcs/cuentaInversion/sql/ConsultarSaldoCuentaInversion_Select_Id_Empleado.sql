SELECT 'EMP' AS "Tipo",E."IdEmpleado"
FROM "BSCL.SocioNegocio.BD::EMP.Empleado" AS E
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS U ON U."IdEntidad" = E."IdEmpleado"
WHERE U."TipoEntidad" = 'EMP' AND "IdUsuario" = :p_IdUsuario
UNION
SELECT 'CLI' AS "Tipo",E."IdCliente"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS E
INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS U ON U."IdEntidad" = E."IdCliente"
WHERE U."TipoEntidad" = 'CLI' AND "IdUsuario" = :p_IdUsuario

