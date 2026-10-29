SELECT
	 T."IdPortafolio"
	,T."IdMoneda"
FROM (
	SELECT
		 POR."IdPortafolio"
		,MON."IdMoneda"
	--FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR
	FROM "BSCL.Plataforma.BD::PLA.ConsultarFlujoCaja_Portafolio_Proceso" PROCESO
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as POR ON POR."IdPortafolio" = PROCESO."IdPortafolio"
	--INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI
		ON (CLI."IdCliente" = POR."IdCliente" AND
			CLI."Borrado" = 'N')
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER
		ON (PER."IdPersona" = CLI."IdPersona" AND
			PER."Borrado"='N')
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT
		ON (PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" AND
			IDENT."Borrado" = 'N')
	INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarCategoriaInversionista" as CAT
		ON (CAT."IdCategoriaInversionista" = CLI."IdCategoriaInversionista" AND
			CAT."IdEmpresa" = CLI."IdEmpresa")
	,"BSCL.Plataforma.BD::PLA.Moneda" as MON
	WHERE POR."IdPortafolio" IS NOT NULL
	  AND (MON."IdMoneda" IN (:IdMoneda) OR 1 = :FiltroMonedaValido)
	) T
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTA
	ON (CTA."IdPortafolio" = T."IdPortafolio" AND CTA."IdMoneda" = T."IdMoneda")
--GROUP BY T."IdPortafolio",T."IdMoneda"
;