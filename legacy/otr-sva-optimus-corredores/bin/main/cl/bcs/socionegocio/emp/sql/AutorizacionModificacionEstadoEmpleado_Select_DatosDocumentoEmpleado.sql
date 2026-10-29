SELECT
	"IdEmpleado" AS "IdEmpleado",
	"TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad",
	"DocumentoIdentidad" AS "DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
WHERE "IdEmpleado" IN (:IdsEmpleado)
