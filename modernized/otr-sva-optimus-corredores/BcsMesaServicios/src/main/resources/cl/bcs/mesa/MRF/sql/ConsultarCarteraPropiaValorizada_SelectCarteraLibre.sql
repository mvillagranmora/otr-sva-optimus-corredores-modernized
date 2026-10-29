SELECT 
	'CARTERA' as "TablaAfecta",
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion",
	CAR."IdCartera",
	CAR."FolioCartera",
	CAR."Nemotecnico",
	CAR."CodigoIFRS",
	CAR."IdEmisor",
	CAR."EsFungible",
	TO_VARCHAR(CAR."FechaVencimientoInstrumento",'YYYY-MM-DD')  AS "FechaVencimientoInstrumento",
	TO_VARCHAR(CAR."FechaVencimientoOperacion",'YYYY-MM-DD')  AS "FechaVencimientoOperacion",
	TO_VARCHAR(CAR."FechaOperacion",'YYYY-MM-DD')  AS "FechaOperacion",
	CAR."INGCantidad" AS "Cantidad",
	CAR."MontoCompra" AS "MontoCompra",
	CAR."IdMonedaInstrumento",
	CAR."INGTasaCompra" AS "TasaActual",
	'CD' AS "Diferenciador",
	CAR."IdCliente" ,
	CAR."IdPortafolio" ,
	CAR."IdClasificacionTipoInstrumento" ,
	CAR."IdTipoInstrumento" ,
	CAR."IdInstrumento" ,
	TO_VARCHAR(CAR."FechaVencimientoInstrumento",'YYYY-MM-DD')  AS "FechaEmisionInstrumento",
	CAR."IdTipoReajuste" AS "IdTipoReajusteInstrumento" ,
	CAR."IdMonedaEmision" ,
	CAR."IdCondicionLiquidacion" ,
	CAR."IdTipoOperacion" ,
	CAR."NemoSVS" ,
	CAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema",
	CAR."INGValorRescate" AS "ValorRescate",
	CAR."INGPrecioCompra"  AS "PrecioCompra",
	CAR."IdMonedaEmision" AS "IdMonedaEmisionInstrumento",
	CAR."IdOperacion",
	CAR."IdBolsa",
	CAR."IdMesa",
	TO_VARCHAR(CAR."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion",
	CAR."IdBoveda",
	CAR."IdTipoProducto"
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
WHERE 
	CAR."IdCliente"= CASE WHEN :p_IdCliente = 0 THEN CAR."IdCliente" ELSE :p_IdCliente END 
	AND CAR."IdPortafolio"= CASE WHEN :p_IdPortafolio = 0 THEN CAR."IdPortafolio" ELSE :p_IdPortafolio END   
	AND CAR."IdEstado"='VIG' 
	AND CAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND CAR."EGRCantidad" = 0
    AND CAR."IdTipoOperacion" != 'COMPAC'
	AND CAR."FechaOperacion" <= :p_Fecha
ORDER BY 
	CAR."IdEmisor",
	CAR."Nemotecnico",
	CAR."FechaVencimientoInstrumento",
	CAR."Cantidad"
DESC