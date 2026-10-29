SELECT 
	--PER."IdPersona" || '_' || POR."IdPortafolio" || '_' || MON."IdMoneda" AS "Id",
	PER."IdPersona",
	CLI."IdCliente",
	POR."IdPortafolio",
	MON."IdMoneda"
FROM "BSCL.Tesoreria.BD::TES.SaldoPendienteDeLiquidar_Persona_Proceso" PROCESO
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = PROCESO."IdPersona"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI
	ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
	ON POR."IdCliente" = CLI."IdCliente"
LEFT JOIN "BSCL.Tesoreria.BD::TES.MonedaTesoreria" AS MON
	ON MON."IdMoneda" = MON."IdMoneda"
WHERE PER."IdPersona" IS NOT NULL;