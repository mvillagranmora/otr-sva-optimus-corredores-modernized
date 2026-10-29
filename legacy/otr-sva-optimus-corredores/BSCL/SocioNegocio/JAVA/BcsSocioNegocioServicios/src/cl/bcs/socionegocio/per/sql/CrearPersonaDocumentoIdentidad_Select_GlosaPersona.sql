SELECT
	"DocumentoIdentidad" AS "DocumentoIdentidad",
	"NombreCompleto" AS "NombreCompleto"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
WHERE "IdPersona" = :IdPersona
