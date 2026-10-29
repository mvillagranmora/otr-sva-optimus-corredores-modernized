SELECT 
GTA."IdPortafolio"
,GTA."IdMonedaInstrumento" AS "IdMoneda"
,SUM(GTA."INGMontoATasaGarantiaActual") AS "SaldoMonedaSistema"
,SUM(GTA."INGMontoGarantiaActualMonedaAdicional") AS "SaldoMonedaAdicional"
FROM "BSCL.Custodia.BD::HIS.Garantia" GTA
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON GTA."IdPortafolio" = P."IdPortafolio"
WHERE
GTA."FechaRespaldo" = :FechaHasta
AND GTA."IdOrigenGarantia" = 'CTA'
AND GTA."IdEstado" = 'VIG'
AND GTA."INGFechaOperacion" BETWEEN :FechaDesde AND :FechaHasta
GROUP BY GTA."IdPortafolio", GTA."IdMonedaInstrumento"