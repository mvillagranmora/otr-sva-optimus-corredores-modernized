SELECT  
--POR."IdPortafolio" || '_' || MON."IdMoneda" AS "Id",
CLI."IdPersona"
,POR."IdPortafolio"
,MON."IdMoneda"
FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" PROCESO
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as POR ON POR."IdPortafolio" = PROCESO."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdCliente" = POR."IdCliente"
,"BSCL.Plataforma.BD::PLA.Moneda" as MON
WHERE POR."IdPortafolio" IS NOT NULL
--GROUP BY CLI."IdPersona",POR."IdPortafolio",MON."IdMoneda"
;