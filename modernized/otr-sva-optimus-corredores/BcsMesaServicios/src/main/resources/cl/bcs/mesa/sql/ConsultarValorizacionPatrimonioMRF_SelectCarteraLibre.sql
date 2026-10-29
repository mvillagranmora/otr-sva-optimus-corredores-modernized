SELECT
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion",
    'CARTERA' as "IdUso",
    'CARTERA' as "TablaOrigen",
    CC."IdCliente",
    CC."FolioCartera" AS "FolioCartera",
 	CC."FolioComprobante" ,
    CC."IdCartera" AS "IdCartera",
    CC."IdTipoInstrumento",
    CC."IdClasificacionTipoInstrumento",
    CC."IdInstrumento",
    TO_VARCHAR(CC."FechaEmisionInstrumento", 'YYYY-MM-DD') AS "FechaEmisionInstrumento",
	TO_VARCHAR(CC."FechaVencimientoOperacion", 'YYYY-MM-DD') AS "FechaVencimientoOperacion",
    CC."IdMonedaInstrumento",
    TO_VARCHAR(CC."FechaVencimientoInstrumento", 'YYYY-MM-DD') AS "FechaVencimientoInstrumento",
    CC."CodigoIFRS",
    CC."IdTipoOperacion",
    CC."INGValorRescate" AS "ValorRescate",
    CC."MontoCompraMonedaInstrumento",
    CC."IdMonedaEmision" AS "IdMonedaEmisionInstrumento",
    CC."IdTipoReajuste" AS "IdTipoReajusteInstrumento" ,
    TO_VARCHAR(CC."FechaOperacion",'YYYY-MM-DD')  AS "FechaOperacion",
    CASE 
	    WHEN TO_VARCHAR(CC."FechaVencimientoOperacion", 'YYYY-MM-DD') = '0001-01-01' 
	        THEN CAST(DAYS_BETWEEN(:p_FechaOperacion, CC."FechaVencimientoInstrumento") AS BIGINT)
	    WHEN TO_VARCHAR(CC."FechaVencimientoOperacion", 'YYYY-MM-DD') != '0001-01-01' 
	        THEN CAST(DAYS_BETWEEN(:p_FechaOperacion, CC."FechaVencimientoOperacion") AS BIGINT)
	    ELSE CAST(0 AS BIGINT)
	END AS "PlazoEnDiasOperacion",
    CC."IdAgente",
    CC."MontoCompraMonedaAdicional",
    CC."IdConcepto",
    CC."IdOperacion" AS "IdOperacion" ,
    CC."INGCantidad" AS "Cantidad",
    CASE  
        WHEN CC."IdClasificacionTipoInstrumento" = 'IF' THEN CC."INGPrecioCompra"
        ELSE CC."PrecioCompra"  
    END AS "PrecioCompra",
    CC."INGTasaCompra" AS "TasaCompra",
    CC."MontoCompra",
    CASE 
        WHEN CC."IdTipoOperacion" = 'COMTER' THEN CC."IdPortafolio"  
        ELSE CC."IdPortafolioRelacionado" 
    END AS "IdPortafolio",
	CC."Nemotecnico",
    CC."NemoSVS",
    CC."IdEmisor",
    'CD' AS "Diferenciador",
    'PROPIA' AS "TipoCartera",  
    CASE  
        WHEN CC."IdClasificacionTipoInstrumento" = 'IF' THEN CC."INGIdCondicionLiquidacion"
        ELSE CC."IdCondicionLiquidacion"  
    END AS "IdCondicionLiquidacion",
    CC."FolioComprobante"  AS "FolioComprobante",
    CC."TipoCarteraCorredor",
    TO_VARCHAR(CC."FechaLiquidacion",'YYYY-MM-DD')  AS "FechaLiquidacion"
FROM "BSCL.Custodia.BD::CUS.Cartera" CC
	LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" AS T_EMI 
    ON (
        T_EMI."IdClasificacionTipoInstrumento" = CC."IdClasificacionTipoInstrumento" 
        AND T_EMI."IdTipoInstrumento" = CC."IdTipoInstrumento"
        AND (
            T_EMI."Nemotecnico" = CASE CC."IdClasificacionTipoInstrumento" WHEN 'IF' THEN LEFT(CC."Nemotecnico", 4) ELSE '' END
            OR T_EMI."Nemotecnico" = CASE CC."IdClasificacionTipoInstrumento" WHEN 'IF' THEN LEFT(CC."Nemotecnico", 3) ELSE '' END
            OR T_EMI."Nemotecnico" = CASE CC."IdClasificacionTipoInstrumento" WHEN 'IF' THEN LEFT(CC."Nemotecnico", 6) ELSE '' END
        )
        AND T_EMI."IdEmisor" = CASE
            WHEN CC."IdClasificacionTipoInstrumento" = 'IF' AND T_EMI."Nemotecnico" = LEFT(CC."Nemotecnico", 6) THEN CC."IdEmisor"
            WHEN CC."IdClasificacionTipoInstrumento" = 'IF' AND T_EMI."Nemotecnico" = '' THEN ''
            ELSE ''
        END
    )
WHERE (CC."IdClasificacionTipoInstrumento" = 'RF' OR CC."IdClasificacionTipoInstrumento" = 'IF')
    AND (
        (:p_plazoDias = 1  AND (DAYS_BETWEEN(TO_SECONDDATE(:p_FechaOperacion, 'YYYY-MM-DD'), TO_SECONDDATE(CC."FechaOperacion", 'YYYY-MM-DD'))) <= 90)
        OR (:p_plazoDias = 2  AND (DAYS_BETWEEN(TO_SECONDDATE(:p_FechaOperacion, 'YYYY-MM-DD'), TO_SECONDDATE(CC."FechaOperacion", 'YYYY-MM-DD'))) > 90)
        OR (:p_plazoDias = 3)
    )
    AND CC."IdCliente" = :p_IdCliente 
    AND ( :p_IdPortafolio = 0 OR :p_IdPortafolio = CASE WHEN CC."IdTipoOperacion" IN ( 'COMPAC', 'COMTER' ) 
    	THEN CC."IdPortafolio"  
        ELSE CC."IdPortafolioRelacionado" 
    END)
    AND CC."IdTipoInstrumento" <> 'PAC'
    AND CC."IdTipoOperacion" <> 'VENTER'
    AND CC."IdEstado" = 'VIG'
    AND CC."IdEmpresa" = :p_IdEmpresa
ORDER BY 
	CC."IdEmisor",
	CC."Nemotecnico",
	CC."FechaVencimientoInstrumento",
	CC."Cantidad"