UPDATE
 	"BSCL.Orden.BD::ORD.Asignacion" Asignacion
SET
	Asignacion."IdEstadoAsignacion" = :IdEstadoAsignacionPostAutorizacion
WHERE
	Asignacion."IdAsignacion" = :IdAsignacion;
