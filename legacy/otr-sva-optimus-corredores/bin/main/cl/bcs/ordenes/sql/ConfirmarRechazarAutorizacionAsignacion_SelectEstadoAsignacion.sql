SELECT
	Asignacion."IdAsignacion" AS "IdAsignacion"
	, Asignacion."IdEstadoAsignacion" AS "IdEstadoAsignacionPreAutorizacion"
FROM
	"BSCL.Orden.BD::ORD.Asignacion" Asignacion
WHERE
	Asignacion."IdAsignacion" IN (:AsignacionIds);