SELECT
	"DocumentoIdentidad" || ' / ' || "NombreCompleto" || ' / CalleNro: ' || :Calle || ' ' || :Numero AS "Glosa"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
WHERE "IdPersona" = :IdPersona
