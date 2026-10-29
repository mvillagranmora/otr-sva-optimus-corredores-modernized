SELECT 	 
	 CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdBoveda"
	,SUB."IdClasificacionTipoInstrumento"
	,SUB."IdTipoInstrumento"
	,SUB."IdInstrumento"
	,SUB."Nemotecnico"	
	,SUM(SUB."INGCantidad")							AS "Disponible"		
	,SUB."IdMonedaInstrumento"
	,SUB."IdEmisor"
	,CASE WHEN CAR."INGPrecioPromedioCompra" <> 0 THEN CAR."INGPrecioPromedioCompra" ELSE CAR."INGPrecioCompra" END		AS "PrecioPromedioCompra"
	,SUM(SUB."MontoPrecioPromedioCompraOperacionRelacionada") 	AS "INGMontoPrecioPromedioCompra"
	,SUM(SUB."MontoPrecioPromedioMonedaAdicionalOperacionRelacionada") 	AS "INGMontoPrecioPromedioMonedaAdicional"
	,SUM(SUB."INGMontoATasaCompraActual") 				AS "INGMontoATasaCompraActual"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND SUB."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND SUB."IdEstado" = 'VIG'
AND CAR."FechaOperacion" <= :FechaHasta
AND CAR."IdTipoInstrumento" = 'PRT'
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor",CAR."INGPrecioPromedioCompra",CAR."INGPrecioCompra"