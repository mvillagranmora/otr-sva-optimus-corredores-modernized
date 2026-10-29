SELECT 	 
	 CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdBoveda"
	,SUB."IdClasificacionTipoInstrumento"
	,SUB."IdTipoInstrumento"
	,SUB."IdInstrumento"
	,SUB."Nemotecnico"	
	,SUM(SUB."INGCantidad")	AS "Disponible"
	,SUB."IdMonedaInstrumento"
	,SUB."IdEmisor"
	,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
	,SUB."INGPrecioMercadoActual" 						AS "PrecioMercado"
	,SUM(SUB."MontoPrecioPromedioCompraOperacionRelacionada") 			AS "INGMontoPrecioPromedioCompra"
	,SUM(SUB."MontoPrecioPromedioMonedaAdicionalOperacionRelacionada") 	AS "INGMontoPrecioPromedioMonedaAdicional"
	,SUM(SUB."INGMontoATasaCompraActual") 				AS "INGMontoATasaCompraActual"
	,SUM(SUB."INGMontoATasaMercadoActual") 				AS "INGMontoATasaMercadoActual"
	,SUM(SUB."INGMontoMercadoActualMonedaAdicional") 	AS "INGMontoMercadoActualMonedaAdicional"
FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" AND SUB."FechaRespaldo" = CAR."FechaRespaldo"
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND CAR."FechaRespaldo" = :FechaHasta
AND SUB."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND SUB."IdEstado" = 'VIG'
AND CAR."FechaOperacion" <= :FechaHasta
AND CAR."IdTipoInstrumento" IN ('SIM','PAC','RRV','RRF')
AND CAR."IdTipoOperacion" = 'VENSIM'
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",SUB."IdClasificacionTipoInstrumento",
SUB."IdTipoInstrumento",SUB."IdInstrumento",SUB."Nemotecnico",SUB."IdMonedaInstrumento",SUB."IdEmisor",CAR."INGPrecioPromedioCompra",SUB."INGPrecioMercadoActual"