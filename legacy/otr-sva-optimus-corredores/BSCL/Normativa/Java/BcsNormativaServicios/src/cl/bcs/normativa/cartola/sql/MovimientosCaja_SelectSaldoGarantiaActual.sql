SELECT 
GTA."IdPortafolio"
,GTA."IdMonedaInstrumento" AS "IdMoneda"
,SUM(GTA."INGMontoGarantiaActualMonedaInstrumento") AS "SaldoMonedaInstrumento"
,SUM(GTA."INGMontoATasaGarantiaActual") AS "SaldoMonedaSistema"
,SUM(GTA."INGMontoGarantiaActualMonedaAdicional") AS "SaldoMonedaAdicional"
FROM "BSCL.Custodia.BD::GTA.Garantia" GTA
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON GTA."IdPortafolio" = P."IdPortafolio"
WHERE
GTA."IdOrigenGarantia" = 'CTA'
AND GTA."IdEstado" = 'VIG'
AND GTA."INGFechaOperacion" BETWEEN :FechaDesde AND :FechaHasta
GROUP BY GTA."IdPortafolio", GTA."IdMonedaInstrumento"