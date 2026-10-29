SELECT
	"IdEstado" AS "IdEstado",
	"IdTipoBloqueo" AS "IdTipoBloqueo",
	"IdTipoDesbloqueo" AS "IdTipoDesbloqueo",
	"FechaBloqueo" AS "FechaBloqueo",
	"FechaDesbloqueo" AS "FechaDesbloqueo",
	"ComentarioBloqueo" AS "ComentarioBloqueo",
	"ComentarioDesbloqueo" AS "ComentarioDesbloqueo"
FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
WHERE "IdEmpleado" = :IdEmpleado
