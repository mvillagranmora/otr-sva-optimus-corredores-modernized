SELECT 
CARTERA."IdCartera"
,CARTERA."Nemotecnico" AS "IdSerieFondo"
,CARTERA."INGCantidad"
,FONDO."IdMoneda"
,CARTERA."INGMontoCompraActualMonedaInstrumento"
,CARTERA."INGPrecioCompra"
,CARTERA."EGRCantidad"
,CARTERA."EGRMontoMonedaInstrumento"
,CARTERA."EGRPrecio"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CARTERA
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdInstrumento" = CARTERA."IdInstrumento"
INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (Fondo."IdFondo" = SerieDeFondo."IdFondo")
WHERE CARTERA."IdCartera" IN (:IdCartera)
AND CARTERA."IdEstado" IN ('RPA','LIQ')