WITH pactos AS (
    SELECT 
	    'CARTERA' as "IdUso",
	    'CARTERA' as "TablaOrigen",
        CC."IdCliente",
        CC."FolioCartera" AS "FolioCartera",
        OP."FolioOperacion" AS "FolioOperacion",
        CC."IdCartera" AS "IdCartera",
        CC."INGTasaMercadoActual" AS "TIRMercado",
        CC."IdTipoInstrumento",
        CC."IdClasificacionTipoInstrumento",
        CC."IdInstrumento",
        TO_VARCHAR(CC."FechaEmisionInstrumento", 'YYYY-MM-DD') AS "FechaEmisionInstrumento",
        TO_VARCHAR(CC."FechaVencimientoOperacion", 'YYYY-MM-DD') AS "FechaVencimientoOperacion",
        TO_VARCHAR(CC."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion",
        CC."IdMonedaInstrumento",
        TO_VARCHAR(CC."FechaVencimientoInstrumento", 'YYYY-MM-DD') AS "FechaVencimientoInstrumento",
        CC."CodigoIFRS",
        CC."EsFungible",
        CC."IdTipoOperacion",
        CC."ValorRescate",
        CC."MontoCompraMonedaInstrumento",
        CC."IdMonedaEmision" AS "IdMonedaEmisionInstrumento",
        CC."IdTipoReajuste" AS "IdTipoReajusteInstrumento",
        TO_VARCHAR(CC."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion",
        DAYS_BETWEEN(TO_DATE(:p_FechaHoraSistema, 'YYYY-MM-DD'), TO_DATE(CC."FechaVencimientoInstrumento", 'YYYY-MM-DD')) AS "FechaPlazoEfectiva",
        CASE 
            WHEN TO_VARCHAR(CC."FechaVencimientoOperacion", 'YYYY-MM-DD') = '0001-01-01' 
                THEN CAST(DAYS_BETWEEN(TO_DATE(:p_FechaOperacion, 'YYYY-MM-DD'), TO_DATE(CC."FechaVencimientoInstrumento", 'YYYY-MM-DD')) AS BIGINT)
            WHEN TO_VARCHAR(CC."FechaVencimientoOperacion", 'YYYY-MM-DD') != '0001-01-01' 
                THEN CAST(DAYS_BETWEEN(TO_DATE(:p_FechaOperacion, 'YYYY-MM-DD'), TO_DATE(CC."FechaVencimientoOperacion", 'YYYY-MM-DD')) AS BIGINT)
            ELSE CAST(0 AS BIGINT)
        END AS "PlazoEnDiasOperacion",
        CC."INGDurationCompraActual" AS "Duration",
        CASE 
            WHEN CC."IdMonedaInstrumento" <> 'CLP' THEN CC."INGMontoMercadoActualMonedaInstrumento" 
            ELSE CC."INGMontoATasaMercadoActual" 
        END AS "MontoValorActual",
        CC."INGPrecioCompra" AS "PrecioProximo",
        CC."IdAgente",
        CC."MontoCompraMonedaAdicional",
        CC."IdConcepto",
        CC."IdOperacion" AS "IdOperacion",
        CASE 
            WHEN :p_TipoValor = 2 AND :p_plazoDias = 2 THEN CC."INGMontoATasaMercadoActual"
            ELSE CC."INGMontoMercadoActualMonedaInstrumento"
        END AS "ValorMercado",
        CASE
            WHEN :p_TipoValor = 2 AND :p_plazoDias = 2 AND CC."INGTasaMercadoActual" > 0 THEN CC."INGTasaMercadoActual"
            WHEN :p_TipoValor = 2 AND :p_plazoDias = 2 AND CC."INGTasaMercadoActual" = 0 THEN CC."INGTasaCompra"
            ELSE CC."INGTasaCompra"
        END AS "TIR",
        CASE 
            WHEN :p_TipoValor = 1 THEN 'COMP'
            ELSE 'MERC'
        END AS "TipoValorizacion",
        DAYS_BETWEEN(TO_SECONDDATE(:p_FechaHoraSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CC."FechaVencimientoInstrumento", 'YYYY-MM-DD')) AS "Plazo",
        DAYS_BETWEEN(TO_SECONDDATE(:p_FechaHoraSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CC."FechaVencimientoOperacion", 'YYYY-MM-DD')) AS "PlazoPacto",
        CASE  
            WHEN CC."IdClasificacionTipoInstrumento" = 'IF' THEN CC."INGValorRescate"
            ELSE CC."INGCantidad"  
        END AS "Cantidad",
        CASE  
            WHEN CC."IdClasificacionTipoInstrumento" = 'IF' THEN CC."INGPrecioCompra"
            ELSE CC."PrecioCompra"  
        END AS "PrecioCompra",
        CC."INGTasaCompra" AS "TasaCompra",
        CC."MontoCompra",
        CASE 
            WHEN OP."IdTipoOperacion" IN ('COMPAC', 'COMTER') THEN CC."IdPortafolio"  
            ELSE CC."IdPortafolioRelacionado" 
        END AS "IdPortafolio",
		CASE 
	    	WHEN CC."IdClasificacionTipoInstrumento"='IF' THEN CC."NemoSVS" 
	    	ELSE CC."Nemotecnico" 
	    END AS "Nemotecnico",
        CC."NemoSVS",
        CC."IdEmisor",
        CASE
            WHEN (OP."IdTipoOperacion" IN ('VENPAC', 'COMPAC') AND CC."TipoCarteraCorredor" <> 'CC') OR (OP."IdTipoOperacion" = 'COMPAC' AND CC."TipoCarteraCorredor" = 'CC') THEN 'PC'
            WHEN OP."IdTipoOperacion" = 'VENPAC' AND CC."TipoCarteraCorredor" = 'CC' THEN 'PP'
            ELSE 'CD'
        END AS "Diferenciador",
        'PROPIA' AS "TipoCartera",
        CASE  
            WHEN CC."IdClasificacionTipoInstrumento" = 'IF' THEN CC."INGIdCondicionLiquidacion"
            ELSE CC."IdCondicionLiquidacion"  
        END AS "IdCondicionLiquidacion",
        COALESCE( OP."FolioOperacion", 0) AS "FolioComprobante",
   		 CC."TipoCarteraCorredor"
    FROM "BSCL.Mesa.BD::MES.Operacion" OP
    INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RSV 
        ON OP."IdOperacion" = RSV."IdOperacion" AND RSV."IdCarteraCustodia" > 0
    INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CC
        ON RSV."IdCarteraCustodia" = CC."IdCartera"
    WHERE OP."IdTipoOperacion" IN ('COMPAC', 'VENPAC')
    AND OP."IdEstado" = 'VIG'
    AND OP."IdTipoProducto" = 'MERF'
    AND (:p_IdCliente = 0 OR :p_IdCliente = OP."IdClienteCorredor")
    AND (:p_IdPortafolio = 0 OR :p_IdPortafolio = OP."IdPortafolioCorredor")
    AND (
        (:p_plazoDias = 1 AND DAYS_BETWEEN(TO_SECONDDATE(:p_FechaOperacion, 'YYYY-MM-DD'), OP."FechaVencimiento") <= 90)
        OR (:p_plazoDias = 2 AND DAYS_BETWEEN(TO_SECONDDATE(:p_FechaOperacion, 'YYYY-MM-DD'), OP."FechaVencimiento") > 90)
        OR (:p_plazoDias = 3)
    )
    AND OP."IdEmpresa" = :p_IdEmpresa
)
SELECT "BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion", *
FROM pactos CP
WHERE CP."IdTipoOperacion" = 'COMPAC'
AND EXISTS (
    SELECT 1 
    FROM pactos VP
    WHERE VP."IdTipoOperacion" = 'VENPAC'
    AND VP."Nemotecnico" = CP."Nemotecnico"
    AND VP."IdEmisor" = CP."IdEmisor"
    AND VP."IdClasificacionTipoInstrumento" = CP."IdClasificacionTipoInstrumento"
    AND VP."IdTipoReajusteInstrumento" = CP."IdTipoReajusteInstrumento"
    AND VP."FechaVencimientoInstrumento" = CP."FechaVencimientoInstrumento"
    AND TRIM(VP."Cantidad") = TRIM(CP."Cantidad")
);