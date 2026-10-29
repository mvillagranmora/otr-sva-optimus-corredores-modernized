SELECT 	 
	 CAR."IdCliente"
	,CAR."IdPortafolio"
	,CAR."IdBoveda"
	,CAR."IdClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"
	,CAR."IdInstrumento"
	,CAR."Nemotecnico"	
	,SUM(CAR."INGCantidad")	AS "Disponible"			
	,CAST(0 as DECIMAL(26,8)) AS "DisponibleTra"
	,CAR."IdMonedaInstrumento"
	,CAR."IdEmisor"
	--------------------------------------------------------------------------------------------
	,CAR."INGPrecioPromedioCompra" 						AS "PrecioPromedioCompra"
	,SUM(CAR."INGMontoPrecioPromedioCompra") 			AS "INGMontoPrecioPromedioCompra"
	,SUM(CAR."INGMontoPrecioPromedioMonedaAdicional") 	AS "INGMontoPrecioPromedioMonedaAdicional"
	,SUM(CAR."INGMontoATasaCompraActual") 				AS "INGMontoATasaCompraActual"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND CAR."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND CAR."FechaOperacion" <= :FechaHasta
AND CAR."EGRCantidad" = 0
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."INGPrecioPromedioCompra"

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
	,SUM(CAR."PrecioOperacion") 	AS "INGMontoPrecioPromedioCompra"
	,SUM(CAR."PrecioOperacion") 	AS "INGMontoPrecioPromedioMonedaAdicional"
	,SUM(CAR."MontoOperacionMonedaInstrumento") 	AS "INGMontoATasaCompraActual"
FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" CAR
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON ins."IdInstrumento" = car."IdInstrumento" 
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND INS."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdEstado" = 'VIG'
AND CAR."FechaOperacion" <= :FechaHasta
AND INS."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",ins."IdClasificacionTipoInstrumento",
ins."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."PrecioOperacion"

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
	--------------------------------------------------------------------------------------------
	,CAR."PrecioCompra"										AS "PrecioPromedioCompra"
	,SUM(CAR."MontoCompra") 								AS "INGMontoPrecioPromedioCompra"
	,SUM(CAR."MontoCompraMonedaInstrumento") 				AS "INGMontoPrecioPromedioMonedaAdicional"
	,SUM(CAR."MontoCompra")									AS "INGMontoATasaCompraActual"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS CAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" AS PORTAFOLIO_PASO ON (CAR."IdPortafolio" = PORTAFOLIO_PASO."IdPortafolio")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoOp ON CAR."IdTipoOperacion" = tipoOp."IdTipoOperacion"
WHERE PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND CAR."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdEstado" = 'PEN'
AND CAR."FechaOperacion" <= :FechaHasta
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF', 'PRF', 'ERF')
AND tipoOp."AccionOperacion" NOT IN ('VENTA','RETIRO')
--AND CAR."EsVentaDescubierta" = 'S'
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."PrecioCompra" 
