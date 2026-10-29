

SELECT 
	 SerieDeFondo."IdSerieFondo"
	,CARTERA."IdCartera"
	,CARTERA."INGCantidad"
	,FONDO."IdMoneda"


	,CARTERA."INGMontoCompraActualMonedaInstrumento"
	,CARTERA."INGPrecioCompra"


FROM "BSCL.Custodia.BD::CUS.Cartera" CARTERA
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdInstrumento" = CARTERA."IdInstrumento"
INNER JOIN  "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON (Fondo."IdFondo" = SerieDeFondo."IdFondo")
WHERE CARTERA."IdEstado" = 'VIG';