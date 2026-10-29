SELECT 
"FolioPrestamo"
,"FechaLiquidacion"
,"NemoSubyacente" AS "Nemotecnico"
,"Cantidad"
,"CantidadAsignada"
,"CantidadPorAsignar"
,"RazonSocial"
,"IdMonedaSistema"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
WHERE 
"IdEstado" = 'VIG'
AND "FechaLiquidacion" <= :FechaLiquidacion 