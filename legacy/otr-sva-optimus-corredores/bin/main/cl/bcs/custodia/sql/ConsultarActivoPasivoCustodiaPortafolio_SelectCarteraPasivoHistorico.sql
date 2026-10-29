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
AND tipoOp."AccionOperacion" NOT IN ('COMPRA','INGRESO')
GROUP BY 
CAR."IdCliente",CAR."IdPortafolio",CAR."IdBoveda",CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",CAR."IdInstrumento",CAR."Nemotecnico",CAR."IdMonedaInstrumento",CAR."IdEmisor", CAR."PrecioCompra" 