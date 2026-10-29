SELECT   
--POR."IdPortafolio" || '_' || MDA."IdMoneda" AS "Id",
CLI."IdPersona"
,POR."IdPortafolio"
,CTA."IdMoneda"
FROM "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" PROCESO
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTA ON CTA."IdPortafolio" = PROCESO."IdPortafolio"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (CTA."IdPortafolio" = POR."IdPortafolio")
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON (CLI."IdCliente" = POR."IdCliente")
INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MDA ON (MDA."IdMoneda" = CTA."IdMoneda")
WHERE CTA."IdPortafolio" IS NOT NULL AND MDA."IdMoneda" IN (:IdMoneda);