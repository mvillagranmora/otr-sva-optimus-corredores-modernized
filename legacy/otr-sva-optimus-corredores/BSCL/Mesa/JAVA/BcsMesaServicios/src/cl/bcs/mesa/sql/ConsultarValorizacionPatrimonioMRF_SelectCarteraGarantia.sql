SELECT
	"BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva".NEXTVAL AS "IdValorizacion",
	'CARTERA' as "IdUso",
    'GARANTIAS' as "TablaOrigen", 
    CC."IdCliente",
    CC."FolioCartera" AS "FolioCartera",
    OP."FolioOperacion",
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
    GAR."INGValorRescate" AS "ValorRescate",
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
    CASE  
        WHEN CC."IdClasificacionTipoInstrumento" = 'IF' THEN GAR."INGValorRescate"
        ELSE GAR."INGCantidad"  
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
    CC."Nemotecnico",
    CC."NemoSVS",
    CC."IdEmisor",
	'CG' AS "Diferenciador",  
    'PROPIA' AS "TipoCartera", 
    CASE  
        WHEN CC."IdClasificacionTipoInstrumento" = 'IF' THEN CC."INGIdCondicionLiquidacion"
        ELSE CC."IdCondicionLiquidacion"  
    END AS "IdCondicionLiquidacion",
    COALESCE( OP."FolioOperacion", CC."FolioComprobante" ) AS "FolioComprobante",
	'' AS "TipoCarteraCorredor",
	TO_NVARCHAR(CC."FechaLiquidacion", 'YYYY-MM-DD') AS "FechaLiquidacion"
FROM  "BSCL.Custodia.BD::CUS.Cartera" CC
	INNER JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiasSaldos" Gar ON (
		Gar."FolioCarteraOrigen" = CC."FolioCartera"
		AND CC."IdCliente" = Gar."IdCliente" 
		AND Gar."FolioComprobanteOperacionOrigen" = CC."FolioComprobante" 
		AND Gar."IdCarteraOrigen" = CC."IdCartera")
	LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" OP  ON  CC."FolioComprobante" = OP."IdOperacion" AND OP."IdEstado" = 'VIG' AND OP."IdTipoProducto" = 'MERF'
 WHERE  CC."IdClasificacionTipoInstrumento" IN ('RF','IF')
	AND (
	    (:p_plazoDias = 1  AND (DAYS_BETWEEN(TO_SECONDDATE(:p_FechaOperacion, 'YYYY-MM-DD'), TO_SECONDDATE(CC."FechaOperacion", 'YYYY-MM-DD'))) <= 90)
		OR 
		(:p_plazoDias = 2  AND (DAYS_BETWEEN(TO_SECONDDATE(:p_FechaOperacion, 'YYYY-MM-DD'), TO_SECONDDATE(CC."FechaOperacion", 'YYYY-MM-DD'))) > 90)	        
		OR 
		(:p_plazoDias = 3)
	 )
	AND ( :p_IdCliente = 0 OR :p_IdCliente = CC."IdCliente" )
	AND ( :p_IdPortafolio= 0 OR :p_IdPortafolio = CC."IdPortafolio" )
	AND CC."IdTipoInstrumento" <> 'PAC'
	AND (CC."IdEstado" = 'RPA' OR CC."IdEstado" = 'LIQ')
	AND CC."IdEmpresa" = :p_IdEmpresa
ORDER BY 
	CC."IdEmisor",
	CC."Nemotecnico",
	CC."FechaVencimientoInstrumento"