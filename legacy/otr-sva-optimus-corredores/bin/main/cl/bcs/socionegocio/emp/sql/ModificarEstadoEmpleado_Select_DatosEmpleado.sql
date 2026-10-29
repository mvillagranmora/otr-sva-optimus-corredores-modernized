SELECT
	"NombreCompleto" AS "NombreCompleto",
	"DocumentoIdentidad" AS "DocumentoIdentidad",
	"TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
WHERE "IdEmpleado" = :IdEmpleado
