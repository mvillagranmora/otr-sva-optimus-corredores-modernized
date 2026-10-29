SELECT 	 
 CAR."IdCliente"
,CAR."IdPortafolio"
,CAR."IdBoveda"
,GAR."IdClasificacionTipoInstrumento"
,GAR."IdTipoInstrumento"
,GAR."IdInstrumento"
,GAR."Nemotecnico"	
,SUM(GAR."INGCantidad")								AS "Disponible"			
,GAR."IdMonedaInstrumento"
,GAR."IdEmisor"
,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
,SUM(CAR."INGMontoPrecioPromedioCompra") 			AS "INGMontoPrecioPromedioCompra"
,SUM(CAR."INGMontoPrecioPromedioMonedaAdicional") 	AS "INGMontoPrecioPromedioMonedaAdicional"
,SUM(CAR."INGMontoATasaCompraActual") 				AS "INGMontoATasaCompraActual"
FROM "BSCL.Custodia.BD::GTA.Garantia" GAR
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON GAR."IdCarteraOrigen" = CAR."IdCartera"
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND GAR."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEmpresa" = :IdEmpresa
AND GAR."IdEstado" = 'VIG'
AND GAR."FechaOperacion" <= :FechaHasta
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",GAR."IdClasificacionTipoInstrumento",
GAR."IdTipoInstrumento",GAR."IdInstrumento",GAR."Nemotecnico",GAR."IdMonedaInstrumento",GAR."IdEmisor",CAR."INGPrecioPromedioCompra"