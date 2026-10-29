SELECT 
	'GARANTIA' as "TablaAfecta",
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion",
	GAR."IdGarantia" AS "IdCartera",
	GAR."FolioGarantia" AS "FolioCartera",
	GAR."Nemotecnico",
	GAR."CodigoIFRS",
	GAR."IdEmisor",
	GAR."EsFungible",
	TO_VARCHAR(GAR."FechaVencimientoInstrumento",'YYYY-MM-DD')  AS "FechaVencimientoInstrumento",
	TO_VARCHAR(GAR."FechaVencimientoOperacion",'YYYY-MM-DD')  AS "FechaVencimientoOperacion",
	TO_VARCHAR(GAR."FechaOperacion",'YYYY-MM-DD')  AS "FechaOperacion",
	GAR."INGCantidad" AS "Cantidad",
	GAR."MontoGarantia" AS "MontoCompra",
	GAR."IdMonedaInstrumento",
	GAR."TasaCompraOperacionOrigen" AS "TasaActual",
	'CG' AS "Diferenciador",
	GAR."IdCliente" ,
	GAR."IdPortafolio" ,
	GAR."IdClasificacionTipoInstrumento" ,
	GAR."IdTipoInstrumento" ,
	GAR."IdInstrumento" ,
	TO_VARCHAR(GAR."FechaVencimientoInstrumento",'YYYY-MM-DD')  AS "FechaEmisionInstrumento",
	GAR."IdTipoReajuste" AS "IdTipoReajusteInstrumento" ,
	GAR."IdMonedaEmision" ,
	GAR."IdCondicionLiquidacion" ,
	CAR."IdTipoOperacion" ,
	GAR."NemoSVS" ,
	GAR."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema",
	GAR."INGValorRescate" AS "ValorRescate",
	GAR."PrecioGarantia" AS "PrecioCompra",
	GAR."IdMonedaEmision" AS "IdMonedaEmisionInstrumento",
	GAR."EGRIdOperacion" AS "IdOperacion",
	'' AS "IdBolsa",
	CAST(0 AS BIGINT) AS "IdMesa",
	TO_VARCHAR(GAR."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion",
	CAR."IdBoveda",
	CAR."IdTipoProducto"
FROM  
	"BSCL.Custodia.BD::HIS.Garantia" AS GAR
LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen" AND CAR."FechaRespaldo" = :p_Fecha
WHERE 
	GAR."IdCliente"= :p_IdCliente 
	AND GAR."IdPortafolio"= CASE WHEN :p_IdPortafolio = 0 THEN GAR."IdPortafolio" ELSE :p_IdPortafolio END   
	AND GAR."IdEstado"='VIG' 
	AND GAR."IdClasificacionTipoInstrumento" in ('RF','IF')
	AND GAR."EGRCantidad" = 0			
	AND GAR."FechaRespaldo" = :p_Fecha
	AND GAR."FechaOperacion" <= :p_Fecha
ORDER BY 
	GAR."IdEmisor",
	GAR."Nemotecnico",
	GAR."FechaVencimientoInstrumento",
	GAR."Cantidad"
DESC