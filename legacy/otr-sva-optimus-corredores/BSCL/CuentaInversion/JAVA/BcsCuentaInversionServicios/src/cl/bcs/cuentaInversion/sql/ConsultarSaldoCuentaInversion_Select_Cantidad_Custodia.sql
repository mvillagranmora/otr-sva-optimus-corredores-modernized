
  
SELECT C."IdCliente", C."IdPortafolio",C."Nemotecnico", C."IdMonedaInstrumento" AS "IdMoneda", SUM("INGCantidad") AS "CantidadCustodia"  
FROM "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" CP
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" C ON  CP."IdPortafolio" = C."IdPortafolio"  AND CP."IdMoneda" = C."IdMonedaInstrumento" AND C."IdEstado" = 'VIG'
WHERE C."Nemotecnico" IN (:Nemotecnico) AND C."IdClasificacionTipoInstrumento" = 'FM'
GROUP BY C."IdCliente", C."IdPortafolio", C."IdMonedaInstrumento", C."Nemotecnico"

