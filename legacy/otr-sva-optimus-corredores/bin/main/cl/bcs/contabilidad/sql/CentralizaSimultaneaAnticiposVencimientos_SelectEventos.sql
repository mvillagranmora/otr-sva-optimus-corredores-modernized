WITH v_TabEventosPaso AS (
    SELECT
        TO_BIGINT(:p_IdTipoCentralizacion) AS "IdTipoCentralizacion",
        "IdCartera"                        AS "IdMovimiento",
        "EGRIdConcepto",
        "EGRIdTipoOperacion",
        "EGRMontoARecibirAnticipo",
        "INGMontoAPlazo",
        "EGRMonto",
        "INGCantidad",
        "PrecioCompra",
        "EGRCantidad",
        "IdMonedaInstrumento"              AS "CodigoMoneda"
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
	WHERE ConsultarCartera."IdEmpresa"      = :p_IdEmpresa
	  AND "EGRFechaOperacion"               = :p_FechaContable
      AND ("EGRIdConcepto" IN ('VCTSIC','VCTSIV','ANTSIC','ANTSIV') OR "EGRIdTipoOperacion" IN ('VENSIM','COMSIM'))
      AND "IdClasificacionTipoInstrumento"  = 'RV' 
      AND "IdTipoInstrumento"               = 'SIM'
      AND ConsultarCartera."IdEstado"      != 'ANU'
)

SELECT
    "IdTipoCentralizacion",
    "IdMovimiento",
    CASE "EGRIdConcepto"
        WHEN 'VCTSIC' THEN 'MONTO_VENCIMIENTO_SIMULTANEA_COMPRA'
        WHEN 'VCTSIV' THEN 'MONTO_VENCIMIENTO_SIMULTANEA_VENTA'
        WHEN 'ANTSIC' THEN 'MONTO_ANTICIPO_SIMULTANEA_COMPRA'
        WHEN 'ANTSIV' THEN 'MONTO_ANTICIPO_SIMULTANEA_VENTA'
        ELSE CASE "EGRIdTipoOperacion" 
                WHEN 'VENSIM' THEN 'MONTO_VENCIMIENTO_SIMULTANEA_VENTA' 
                WHEN 'COMSIM' THEN 'MONTO_VENCIMIENTO_SIMULTANEA_COMPRA'
                ELSE '??????' 
             END             
    END AS "IdEvento",
    CASE "EGRIdConcepto"
        WHEN 'VCTSIC' THEN 'Monto Vencimiento Simultanea Compra'
        WHEN 'VCTSIV' THEN 'Monto Vencimiento Simultanea Venta'
        WHEN 'ANTSIC' THEN 'Monto Anticipo Simultanea Compra'
        WHEN 'ANTSIV' THEN 'Monto Anticipo Simultanea Venta'
        ELSE CASE "EGRIdTipoOperacion" 
                WHEN 'VENSIM' THEN 'Monto Vencimiento Simultanea Venta' 
                WHEN 'COMSIM' THEN 'Monto Vencimiento Simultanea Compra'
                ELSE '??????' 
             END            
    END AS "Nombre",
    CASE 
        WHEN "EGRIdConcepto" IN ('ANTSIC', 'ANTSIV') THEN ROUND(ABS("EGRMontoARecibirAnticipo"),0)
        WHEN "EGRIdConcepto" IN ('VCTSIC', 'VCTSIV', 'VNSIMU') THEN ROUND(ABS("INGMontoAPlazo") ,0)
        ELSE CASE  
                WHEN "EGRIdTipoOperacion" IN ('VENSIM', 'COMSIM') THEN ROUND(ABS("EGRMonto") ,0)
             END 
    END AS "MontoEvento",
    "CodigoMoneda"
FROM v_TabEventosPaso
UNION ALL
SELECT
    "IdTipoCentralizacion",
    "IdMovimiento",
    CASE
        WHEN "EGRIdTipoOperacion" IN ('COMSIM', 'ANSIMC') THEN 'MONTO_COMPRA_SIMULTANEA_COMPRA' 
        ELSE 'MONTO_COMPRA_SIMULTANEA_VENTA' 
    END AS "IdEvento",
    CASE 
	    WHEN "EGRIdTipoOperacion" IN ('COMSIM', 'ANSIMC') THEN 'Monto Compra Simultanea Compra' 
	    ELSE 'Monto Compra Simultanea Venta' 
	END AS "Nombre",
	CASE 
		WHEN "EGRIdTipoOperacion" IN ('COMSIM', 'VENSIM') THEN ROUND(ABS("INGCantidad" * "PrecioCompra"), 0)
		ELSE ROUND(ABS("EGRCantidad" * "PrecioCompra"), 0)
	END AS "MontoEvento",
    "CodigoMoneda"
FROM v_TabEventosPaso
WHERE "EGRIdTipoOperacion" IN ('COMSIM', 'VENSIM', 'ANSIMC', 'ANSIMV');