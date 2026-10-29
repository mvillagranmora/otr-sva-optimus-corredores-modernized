SELECT to_bigint(COUNT (DISTINCT P."IdPortafolio")) AS "CantidadCuentas",
P."CodigoPortafolio" AS "Codigo",
to_bigint(CMA."IdPlanTipoInversion") AS "IdPlanTipoInversion", 
PER."Sexo" AS "Sexo"
FROM "BSCL.Portafolio.BD::POR.Portafolio" P
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" 	CMA ON (CMA."IdMatrizAgrupadorAPV" = P."IdMatrizAgrupadorAPV")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON ( P."IdCliente" = CLI."IdCliente" )
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON ( PER."IdPersona" = CLI."IdPersona" )
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 		SAR ON (SAR."Estado" = 'CRT' AND SAR."IdRegimenTributario" IN ('APV-A','APV-B') AND 
																			SAR."FechaCertificacion" <= :FechaFinBusqueda AND SAR."FechaLiquidacion" <= :FechaFinBusqueda AND
																			SAR."IdPortafolio" = P."IdPortafolio")		
WHERE P."IdEstado" = 'ACT' AND
	P."CodigoPortafolio" in ('APV-A','APV-B') AND
	P."FechaCreacion" <= :FechaFinBusqueda
GROUP BY P."CodigoPortafolio",CMA."IdPlanTipoInversion",PER."Sexo";