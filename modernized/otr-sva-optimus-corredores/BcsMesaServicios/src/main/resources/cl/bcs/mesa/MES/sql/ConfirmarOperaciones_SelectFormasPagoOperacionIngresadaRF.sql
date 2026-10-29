SELECT 
"IdComprobantePagoOperacion",
"IdFormaDePago",
"IdMoneda",
"Monto" AS "MontoFormaDePago",
"RecibePaga" AS "RecibePagaPortafolio",
"AfectarTesoreria",
"IdCuentaBanco",
"FechaLiquidacionComprobantePagoOperacion"
FROM  "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" WHERE "IdOperacion" = :IdOperacion;