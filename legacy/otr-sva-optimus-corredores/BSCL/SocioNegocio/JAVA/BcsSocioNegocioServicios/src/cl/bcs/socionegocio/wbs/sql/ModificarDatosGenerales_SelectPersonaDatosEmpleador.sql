SELECT	
	 PER."IdPersona" AS "IdEmpleador"
	,PER."Tipo"AS  "IdTipoPersona"
	,DOCI."IdTipoDocumentoIdentidad" AS "TipoDocumentoIdentidad"
	,DOCI."DocumentoIdentidad" AS "DocumentoIdentidad"
	,PER."Nombres" AS "Nombres"
	,PER."ApellidoPaterno" AS "ApellidoPaterno"
	,PER."ApellidoMaterno" AS "ApellidoMaterno"
	,PER."RazonSocial" AS "RazonSocial"
	,COALESCE(PER."IdTipoCanalIngresoCorredor", '') AS "IdTipoCanalIngresoCorredor"
FROM "BSCL.SocioNegocio.BD::PER.Persona" AS PER
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCI ON PER."IdPersona" = DOCI."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdPersona" = PER."IdPersona"
WHERE DOCI."DocumentoIdentidad" = (:DocumentoIdentidad);