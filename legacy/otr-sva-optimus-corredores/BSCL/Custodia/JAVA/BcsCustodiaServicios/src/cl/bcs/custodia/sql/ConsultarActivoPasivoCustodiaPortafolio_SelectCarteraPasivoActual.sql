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
WHERE 
PORTAFOLIO_PASO."IdTrabajo" = :IdTrabajo
AND CAR."IdClasificacionTipoInstrumento" = 'RV'
AND CAR."IdEmpresa" = :IdEmpresa
AND CAR."IdEstado" = 'PEN'
AND CAR."FechaOperacion" <= :FechaHasta
AND CAR."IdTipoInstrumento" NOT IN ('SIM','PRT','EMP','PAC','RRV','RRF')
AND tipoOp."AccionOperacion" NOT IN ('COMPRA','INGRESO')
--AND CAR."EsVentaDescubierta" = 'S'
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."PrecioCompra" 