SELECT 
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion"
	,RES."IdCarteraOrigen" AS "IdCartera"
	,CASE 
		WHEN OPE."AfectaCustodia" = 'N' THEN RES."FolioCarteraCustodia"
		ELSE RES."FolioCarteraOrigen" 
	END AS "FolioCartera"
	,OPE."IdCliente"
	,OPE."FolioOperacion"
	,OPE."IdPortafolio"
	,RES."IdOperacion"
	,OPE."IdTipoOperacion"
	,'PP' AS "Diferenciador"
	,TO_VARCHAR(SUB."INGFechaOperacion",'YYYY-MM-DD')  AS "FechaOperacion"
	,TO_VARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD') AS "FechaVencimientoOperacion"
	,TO_VARCHAR(OPE."FechaCondicionLiquidacion",'YYYY-MM-DD') AS "FechaLiquidacion"
	,OPE."IdTipoReajuste" AS "IdTipoReajusteInstrumento"
	,SUB."INGCantidad" AS "Cantidad"
	,RES."CodigoIFRS"
	,RES."Nemotecnico"
	,OPE."IdCondicionLiquidacion"
	,SUB."NemoSVS"
	,SUB."IdEmisor"
	,TO_VARCHAR(SUB."FechaVencimientoInstrumento", 'YYYY-MM-DD') AS "FechaVencimientoInstrumento"
	,SUB."IdInstrumento"
	,SUB."IdTipoInstrumento"
	,SUB."IdClasificacionTipoInstrumento"
	,SUB."IdMonedaInstrumento"
	,TO_VARCHAR(SUB."FechaEmisionInstrumento", 'YYYY-MM-DD') AS "FechaEmisionInstrumento"
	,SUB."IdMonedaEmision" AS "IdMonedaEmisionInstrumento"
	,SUB."INGMontoATasaCompraActual" AS "MontoCompra"
	,SUB."INGValorRescate" AS "ValorRescate"
	,SUB."INGPrecioSubyacente" AS "PrecioCompra"
	,OPE."IdAgente"
	,RES."IdConcepto"
	,COALESCE(CAR."TipoCarteraCorredor", '') AS "TipoCarteraCorredor"
	,RES."IdBolsa" 
	,RES."IdBoveda" 
	,OPE."IdTipoProducto" 
	,RES."Tasa" AS "TasaCompra"
	,'PROPIA' AS "TipoCartera"
	,COALESCE(CAR."FolioComprobante", SUB."EGRFolioComprobante") AS "FolioComprobante"
	,'CARTERA' as "IdUso"
	,'CARTERA' AS "TablaOrigen"
	,CASE
		WHEN TO_VARCHAR(CAR."FechaVencimientoOperacion", 'YYYY-MM-DD') = '0001-01-01' 
        	THEN CAST(DAYS_BETWEEN(:p_FechaOperacion, SUB."FechaVencimientoInstrumento") AS BIGINT)
		WHEN TO_VARCHAR(CAR."FechaVencimientoOperacion", 'YYYY-MM-DD') != '0001-01-01'
        	THEN CAST(DAYS_BETWEEN(:p_FechaOperacion,OPE."FechaVencimiento") AS BIGINT)
		ELSE CAST(0 AS BIGINT)
	END AS "PlazoEnDiasOperacion"
FROM "BSCL.Mesa.BD::MES.Operacion" OPE 
	INNER JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON OPE."IdMesaOrigen" = MES."IdMesa"
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS RES ON RES."IdOperacion" = OPE."IdOperacion"
	INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" AS SUB ON SUB."FolioSubyacente" = RES."FolioSubyacente" AND SUB."FechaRespaldo" = :p_FechaOperacion
	LEFT JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR ON RES."IdCarteraOrigen" = CAR."IdCartera" AND CAR."FechaRespaldo" = :p_FechaOperacion
WHERE MES."IdCliente"= :p_IdCliente
	AND MES."IdPortafolio" = CASE WHEN :p_IdPortafolio = 0 THEN MES."IdPortafolio" ELSE :p_IdPortafolio END	
	AND SUB."IdEstado" = 'VIG'
	AND OPE."IdTipoOperacion"='VENPAC'
	AND CAR."TipoCarteraCorredor" = 'CC'
	AND SUB."FechaRespaldo" = :p_FechaOperacion
	AND OPE."FechaOperacion" <= :p_FechaOperacion
	AND (
        (:p_plazoDias = 1  AND (DAYS_BETWEEN(TO_SECONDDATE(:p_FechaOperacion, 'YYYY-MM-DD'), TO_SECONDDATE(CAR."FechaOperacion", 'YYYY-MM-DD'))) <= 90)
        OR 
        (:p_plazoDias = 2  AND (DAYS_BETWEEN(TO_SECONDDATE(:p_FechaOperacion, 'YYYY-MM-DD'), TO_SECONDDATE(CAR."FechaOperacion", 'YYYY-MM-DD'))) > 90)
        OR 
        (:p_plazoDias = 3)
    )
ORDER BY 
	SUB."IdEmisor",
	SUB."Nemotecnico",
	SUB."FechaVencimientoInstrumento",
	CASE WHEN SUB."IdClasificacionTipoInstrumento" = 'IF' THEN SUB."INGValorRescate" ELSE SUB."INGCantidad" END