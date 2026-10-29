SELECT 	 
	 CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdBoveda"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"
	,CAR."IdInstrumento"
	,CAR."Nemotecnico"	
	,SUM(CAR."INGCantidad")	AS "Disponible"	
	,CAST(0 as DECIMAL(26,8))	AS "DisponibleTra"	
	,CAR."IdMonedaInstrumento"
	,CAR."IdEmisor"
	--------------------------------------------------------------------------------------------
	,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
	,CAR."INGPrecioMercadoActual" 						AS "PrecioMercado"
	,SUM(CAR."INGMontoPrecioPromedioCompra") 			AS "INGMontoPrecioPromedioCompra"
	,SUM(CAR."INGMontoPrecioPromedioMonedaAdicional") 	AS "INGMontoPrecioPromedioMonedaAdicional"
	,SUM(CAR."INGMontoATasaCompraActual") 				AS "INGMontoATasaCompraActual"
	,SUM(CAR."INGMontoATasaMercadoActual") 				AS "INGMontoATasaMercadoActual"
	,SUM(CAR."INGMontoMercadoActualMonedaAdicional") 	AS "INGMontoMercadoActualMonedaAdicional"
FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND CAR."FechaRespaldo" = :FechaHasta
AND CAR."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND CAR."FechaOperacion" <= :FechaHasta
AND CAR."EGRCantidad" = 0
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."INGPrecioPromedioCompra", CAR."INGPrecioMercadoActual"

UNION ALL

SELECT 	 
	 CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdBoveda"
	,INS."IdClasificacionTipoInstrumento"
	,INS."IdTipoInstrumento"
	,CAR."IdInstrumento"
	,CAR."Nemotecnico"	
	,-1 * SUM(CAR."Cantidad")	AS "Disponible"			
	,CAST(0 as DECIMAL(26,8)) AS "DisponibleTra"
	,CAR."IdMonedaInstrumento"
	,CAR."IdEmisor"
	--------------------------------------------------------------------------------------------
	,CAR."PrecioOperacion" 			AS "PrecioPromedioCompra"
	,CAR."PrecioMercado"  			AS "PrecioMercado"
	,cast(0 as DECIMAL(26,8))			AS "INGMontoPrecioPromedioCompra"
	,cast(0 as DECIMAL(26,8))	AS "INGMontoPrecioPromedioMonedaAdicional"
	,SUM(CAR."MontoOperacionMonedaInstrumento") 				AS "INGMontoATasaCompraActual"
	,SUM(CAR."MontoMercadoMonedaInstrumento") 				AS "INGMontoATasaMercadoActual"
	,SUM(CAR."MontoMercadoMonedaSistema") 	AS "INGMontoMercadoActualMonedaAdicional"
FROM "BSCL.Custodia.BD::HIS.CarteraNegativa" CAR
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON ins."IdInstrumento" = car."IdInstrumento" 
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND INS."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."FechaRespaldo" = :FechaHasta
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND CAR."FechaOperacion" <= :FechaHasta
AND INS."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF','PRF','ERF')
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",ins."IdClasificacionTipoInstrumento",
ins."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."PrecioOperacion", CAR."PrecioMercado"

UNION ALL

SELECT 	 
	 CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdBoveda"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"
	,CAR."IdInstrumento"
	,CAR."Nemotecnico"	
	,CAST(0 as DECIMAL(26,8)) AS "Disponible"
	,SUM(CAR."Cantidad") AS "DisponibleTra"
	,CAR."IdMonedaInstrumento"
	,CAR."IdEmisor"
	,CAR."PrecioCompra"			AS "PrecioPromedioCompra"
	,CAR."PrecioCompra" 		AS "PrecioMercado"
	,SUM(CAR."MontoCompra") 	AS "INGMontoPrecioPromedioCompra"
	,SUM(CAR."MontoCompraMonedaAdicional") 	AS "INGMontoPrecioPromedioMonedaAdicional"
	,SUM(CAR."MontoCompra") 	AS "INGMontoATasaCompraActual"
	,SUM(CAR."MontoCompra") 	AS "INGMontoATasaMercadoActual"
	,SUM(CAR."MontoCompraMonedaAdicional") 	AS "INGMontoMercadoActualMonedaAdicional"
FROM "BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito" AS CAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS CARHIS ON (CAR."IdMovimientoCarteraEnTransito" = CARHIS."IdMovimientoCarteraEnTransito")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoOp ON CAR."IdTipoOperacion" = tipoOp."IdTipoOperacion"
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND CAR."FechaRespaldo" = :FechaHasta
AND CAR."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdEstado" = 'PEN'
AND CAR."FechaOperacion" <= :FechaHasta
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
--AND CARHIS."EsVentaDescubierta" = 'S'
AND tipoOp."AccionOperacion" NOT IN ('VENTA','RETIRO')
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."PrecioCompra" 