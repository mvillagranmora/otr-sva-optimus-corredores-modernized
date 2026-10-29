SELECT
	  PER."IdPersona" AS "IdPersonaRelacionada"
	, PER."Nombres"
	, PER."ApellidoPaterno"
	, PER."ApellidoMaterno"
	, DOCI."DocumentoIdentidad"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCI ON DOCI."IdPersona" = PER."IdPersona"
WHERE DOCI."DocumentoIdentidad" = :DocumentosIdentidad;