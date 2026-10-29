SELECT
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion"
	,OPE."IdCliente"
	,RES."IdCarteraOrigen" AS "IdCartera"
	,CASE 
		WHEN OPE."AfectaCustodia" = 'N' THEN RES."FolioCarteraCustodia"
		ELSE RES."FolioCarteraOrigen" 
	END AS "FolioCartera"
	,OPE."FolioOperacion"
	,OPE."IdPortafolio"				 
	,OPE."IdOperacion"
	,'PD' AS "Diferenciador"
	,OPE."IdTipoOperacion"
	,TO_VARCHAR(SUB."INGFechaOperacion",'YYYY-MM-DD')  AS "FechaOperacion"
	,TO_VARCHAR(OPE."FechaVencimiento",'YYYY-MM-DD') AS "FechaVencimientoOperacion"
	,TO_VARCHAR(OPE."FechaCondicionLiquidacion",'YYYY-MM-DD') AS "FechaLiquidacion"
	,OPE."IdTipoReajuste"
	,SUB."INGCantidad" AS "Cantidad" 
	,RES."CodigoIFRS"
	,SUB."Nemotecnico"
	,OPE."IdCondicionLiquidacion"
	,SUB."NemoSVS" AS "Instrumento"
	,SUB."IdEmisor"  AS "Emisor"
	,TO_VARCHAR(SUB."FechaVencimientoInstrumento",'YYYY-MM-DD')  AS "FechaVencimientoInstrumento"
	,SUB."IdInstrumento"
	,SUB."IdTipoInstrumento"	
	,SUB."IdClasificacionTipoInstrumento"
	,SUB."IdMonedaInstrumento"
	,TO_VARCHAR(SUB."FechaEmisionInstrumento",'YYYY-MM-DD')  AS "FechaEmisionInstrumento"
	,SUB."IdMonedaEmision" AS "IdMonedaEmisionInstrumento"
	,SUB."INGMontoATasaCompraActual" AS "MontoCompraMonedaSistema"
	,SUB."INGValorRescate"	AS "ValorRescate"
	,SUB."INGPrecioSubyacente"	AS "PrecioCompra"
	,RES."IdConcepto" 
	,CAR."TipoCarteraCorredor" 
	,RES."IdBolsa" 
	,RES."IdBoveda" 
	,OPE."IdTipoProducto" 
	,RES."Tasa" AS "TasaCompra"
	,OPE."IdAgente"
	,SUB."EsFungible"
	,SUB."IdEstado"
	,SUB."IdTipoReajuste" AS "IdTipoReajusteInstrumento"
	,RES."MontoValorizado" AS "MontoOperacion"
	,RES."INGMontoAPlazo" AS "MontoPactado"
	,CASE 
		WHEN :TipoValorizacion = 'C' THEN 
			CASE 
				WHEN :UsaTasaRecompraPacto = 'S' THEN 
					COALESCE(RES."Tasa", CAR."INGTasaCompra")  
				ELSE 
					SUB."TasaCompraOperacionRelacionada"
			END
		WHEN :TipoValorizacion = 'P' THEN SUB."INGTasaSubyacente"
		WHEN (DAYS_BETWEEN(TO_DATE(:FechaSistema, 'YYYY-MM-DD'), TO_DATE(SUB."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 90) 
 				AND SUB."INGTasaMercadoActual" = 0 THEN RES."Tasa"
		ELSE SUB."INGTasaMercadoActual"
	END AS "TasaActual"
FROM 	"BSCL.Mesa.BD::MES.Operacion" OPE 
	INNER JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON OPE."IdMesaOrigen" = MES."IdMesa"
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON SUB."FolioSubyacente" = RES."FolioSubyacente" 
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON RES."IdCarteraOrigen" = CAR."IdCartera" 
WHERE 	MES."IdCliente"= :IdCliente 
	AND MES."IdPortafolio" = CASE WHEN :IdPortafolio = 0 THEN MES."IdPortafolio" ELSE :IdPortafolio END	
	AND SUB."IdEstado" = 'VIG'
	AND OPE."IdTipoOperacion"='COMPAC'
	AND COALESCE(CAR."TipoCarteraCorredor", '') <> 'CC'	
	AND OPE."FechaOperacion" <= :Fecha
ORDER BY 
    SUB."IdEmisor",
    SUB."Nemotecnico",
    SUB."FechaVencimientoInstrumento",
    CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END