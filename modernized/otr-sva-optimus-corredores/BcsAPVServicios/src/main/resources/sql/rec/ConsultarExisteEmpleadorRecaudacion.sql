SELECT 
	"EMP"."IdRecaudacionEmpleador",
	"PER"."DocumentoIdentidad",
	"PER"."IdPersona"
FROM
"BSCL.APV.BD::REC.Empleador" AS "EMP"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"
ON "EMP"."IdPersona" = "PER"."IdPersona"
WHERE "EMP"."IdEmpresa" = :p_IdEmpresa
	AND	"PER"."DocumentoIdentidad" = :p_DoctoIdentidad;