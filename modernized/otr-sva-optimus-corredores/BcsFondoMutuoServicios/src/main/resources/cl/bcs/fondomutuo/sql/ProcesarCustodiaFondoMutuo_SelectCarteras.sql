SELECT 
CARTERA."IdCartera"
,CARTERA."Nemotecnico" AS "IdSerieFondo"
,CARTERA."INGCantidad"
,FONDO."IdMoneda"
,CARTERA."INGMontoCompraActualMonedaInstrumento"
,CARTERA."INGPrecioCompra"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CARTERA
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdInstrumento" = CARTERA."IdInstrumento"
INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (Fondo."IdFondo" = SerieDeFondo."IdFondo")
WHERE CARTERA."FolioCartera" IN (:FolioCartera)
AND CARTERA."IdEstado" = 'VIG'