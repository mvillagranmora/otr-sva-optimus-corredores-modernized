SELECT
	g."IdGarantia"
	,g."INGCantidad" AS "Cantidad"
	,g."INGValorRescate" AS "ValorRescate"
	,g."INGMontoCompraActualMonedaInstrumento" AS "MontoMonedaInstrumento"
	,g."IdCliente"
	,g."IdPortafolio"
	,g."IdGarantia" AS "IdCustodia"
	,c."IdCartera"
	,c."FolioCartera"
	,c."IdOperacion"
	,g."IdConcepto"
	,g."IdInstrumento"
	,g."IdOrigenGarantia"
	,c."FechaVencimientoInstrumento"
	,g."EsFungible"
	,g."IdTipoCanalIngreso" AS "IdTipoCanalIngreso"
	,c."INGIdTipoOperacion" AS "IdTipoOperacion"
	,g."IdMonedaInstrumento" AS "IdMonedaInstrumento"
	,g."INGTasaGarantia" AS "INGTasaGarantia"
	,'' AS "NemoSVS"
	--nuevos
	,g."FechaOperacion"
	,c."IdTipoOperacion"
	,g."IdCondicionLiquidacion"
	,g."FechaLiquidacion"
	,c."IdTipoProducto" 
	,g."IdTipoComprobante" 
	,c."IdBolsa" 	
	,g."Nemotecnico" 
	,g."IdMonedaEmision" 
	,g."IdTipoReajuste" AS "IdTipoReajusteInstrumento"
	,g."IdEmisor" 
	,c."IdBoveda"
	,g."CorteMinimo" 
	,c."PrecioCompra" AS "Precio"
	,c."TasaCompra" AS "Tasa"
	,g."INGMontoATasaCompraActual" AS "MontoCompra"
	,c."MontoCompraMonedaInstrumento" AS "MontoCompraMonedaInstrumento"
	,c."IdClasificacionTipoInstrumento"
	,c."TieneCorteDeCupon"
	,g."MontoGarantiaMonedaInstrumento"
	,c."INGPrecioMercadoActual"
FROM "BSCL.Custodia.BD::GTA.Garantia" g
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" c ON (g."IdCarteraOrigen" = c."IdCartera")
LEFT OUTER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV ON MOV."IdOperacion" = g."IdGarantia" AND MOV."IdTipoComprobante" = 'CUS'
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = g."IdTipoInstrumento" AND TINS."IdClasificacionTipoInstrumento" = 'RF'
where g."IdEstado" = 'VIG' and c."IdTipoInstrumento" != 'PAC' and
c."IdClasificacionTipoInstrumento" = 'RF' and
c."FechaVencimientoInstrumento" > '0001-01-01' and
c."FechaVencimientoInstrumento"<=:Fecha and c."IdEmpresa" = :IdEmpresa
AND MOV."IdOperacion" IS NULL 
