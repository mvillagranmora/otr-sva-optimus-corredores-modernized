SELECT 
	  PER."IdPersona"
	, PER."Nombres"
	, PER."ApellidoPaterno"
	, PER."ApellidoMaterno"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PER
WHERE PER."IdPersona" = :IdPersona;