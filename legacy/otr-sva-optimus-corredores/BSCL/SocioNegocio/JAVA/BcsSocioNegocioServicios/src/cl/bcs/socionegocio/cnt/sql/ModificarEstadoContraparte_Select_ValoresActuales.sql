SELECT
	"IdEstado" AS "IdEstado",
	"IdTipoBloqueo" AS "IdTipoBloqueo",
	CAST("FechaBloqueo" AS NVARCHAR(40)) AS "FechaBloqueo",
	"ComentarioBloqueo" AS "ComentarioBloqueo",
	"IdTipoDesbloqueo" AS "IdTipoDesbloqueo",
	CAST("FechaDesbloqueo" AS NVARCHAR(40)) AS "FechaDesbloqueo",
	"ComentarioDesbloqueo" AS "ComentarioDesbloqueo"
FROM "BSCL.SocioNegocio.BD::CNT.Contraparte"
WHERE "IdContraparte" = :IdContraparte
