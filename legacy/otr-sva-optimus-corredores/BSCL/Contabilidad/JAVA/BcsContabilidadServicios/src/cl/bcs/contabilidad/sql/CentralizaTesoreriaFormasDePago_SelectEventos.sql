SELECT 
    TO_BIGINT(:IdTipoCentralizacion)                      AS "IdTipoCentralizacion",
    MIN(ComprobanteFormaDePago."IdComprobanteFormaDePago")  AS "IdMovimiento",
    CASE Comprobante."IdTipoMovimiento"
        WHEN 'MOVING' THEN 
            CASE 
                WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLICITUD_MONTO_INGRESO'
                ELSE 'SOLICITUD_MONTO_TRASPASO_INGRESO'
            END
        ELSE 
            CASE 
                WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLICITUD_MONTO_EGRESO'
                ELSE 'SOLICITUD_MONTO_TRASPASO_EGRESO'
            END
    END                                                    AS "IdEvento", 
    CASE Comprobante."IdTipoMovimiento"
        WHEN 'MOVING' THEN 
            CASE 
                WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLICITUD MONTO INGRESO'
                ELSE 'SOLICITUD MONTO TRASPASO INGRESO'
            END
        ELSE 
            CASE 
                WHEN Comprobante."ComprobanteNeteado" = 'N' THEN 'SOLICITUD MONTO EGRESO'
                ELSE 'SOLICITUD MONTO TRASPASO EGRESO'
            END
    END                                                    AS "Nombre", 
    SUM(ComprobanteFormaDePago."Monto")                    AS "MontoEvento", 
    Comprobante."IdMoneda"                                 AS "CodigoMoneda"
FROM "BSCL.Tesoreria.BD::TES.Comprobante"                  AS Comprobante
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago  ON ( ComprobanteFormaDePago."IdComprobante" = Comprobante."IdComprobante" 	
  		AND ((ComprobanteFormaDePago."IdEstado" IN ('LIQ','ING') AND :LocSistema = 'CL') OR (ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE'))	
  		and Comprobante."IdConcepto"  NOT IN ('PAREGR','PARING'))
WHERE Comprobante."IdEmpresa" =  :IdEmpresa
	AND (
        (Comprobante."IdEstado" = 'LIQ' AND :LocSistema = 'CL') 
        OR 
        (Comprobante."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')
      )
	AND Comprobante."FechaLiquidacion" = :FechaContable
	AND Comprobante."IdConcepto" NOT IN ('PAREGR', 'PARING')
GROUP BY 
  ComprobanteFormaDePago."NombreCuentaBancariaCorredora",
  Comprobante."IdTipoMovimiento",
  Comprobante."IdEmpresa" ,
  Comprobante."IdComprobante"	,
  Comprobante."FechaLiquidacion",
  Comprobante."IdMoneda",
  Comprobante."IdEstado",
  Comprobante."ComprobanteNeteado"

UNION ALL

-- ============================================
-- Lee Documentos Certificados con valuta
-- ============================================
SELECT 
    TO_BIGINT(:IdTipoCentralizacion)          AS "IdTipoCentralizacion",
    SUB."IdComprobanteFormaDePago" * -1       AS "IdMovimiento",
    CASE SUB."IdTipoMovimiento"
        WHEN 'MOVING' THEN 
            CASE 
                WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO_MONTO_INGRESO'
                ELSE 'DOCUMENTO_MONTO_TRASPASO_INGRESO'
            END
        ELSE 
            CASE 
                WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO_MONTO_EGRESO'
                ELSE 'DOCUMENTO_MONTO_TRASPASO_EGRESO'
            END
    END                                        AS "IdEvento", 
    CASE SUB."IdTipoMovimiento"
        WHEN 'MOVING' THEN 
            CASE 
                WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO MONTO INGRESO'
                ELSE 'DOCUMENTO MONTO TRASPASO INGRESO'
            END
        ELSE 
            CASE 
                WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO MONTO EGRESO'
                ELSE 'DOCUMENTO MONTO TRASPASO EGRESO'
            END
    END                                        AS "Nombre", 
    SUB."Monto"                                AS "MontoEvento", 
    SUB."IdMoneda"                             AS "CodigoMoneda"
FROM (
    SELECT
        Comprobante."IdEstado",
        ComprobanteFormaDePago."FechaIngresoRegistro",
        Comprobante."IdEmpresa",
        ComprobanteFormaDePago."IdComprobanteFormaDePago",
        Comprobante."IdTipoMovimiento",
        ComprobanteFormaDePago."Monto",
        Comprobante."IdMoneda",
        Comprobante."ComprobanteNeteado",
        CAST(
            CASE 
                WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ' 
                    THEN ADD_DAYS(
                        TO_DATE(Comprobante."FechaLiquidacion", 'YYYY-MM-DD'),
                        ComprobanteFormaDePago."DiasRetencion"
                    )
                ELSE ComprobanteFormaDePago."FechaHoraLiquidacion"
            END AS DATE
        ) AS "FechaLiquidacion"
    FROM "BSCL.Tesoreria.BD::TES.Comprobante"             AS Comprobante
    INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago
        ON  ComprobanteFormaDePago."IdComprobante" = Comprobante."IdComprobante"
        AND ComprobanteFormaDePago."IdEstado" IN ('CRT')
    WHERE Comprobante."IdConcepto" NOT IN ('PAREGR', 'PARING')
) SUB
WHERE SUB."IdEmpresa" = :IdEmpresa
	AND (
        (SUB."IdEstado" = 'LIQ' AND :LocSistema = 'CL')
        OR
        (SUB."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')
      )
	AND CAST(SUB."FechaIngresoRegistro" AS DATE) = :FechaContable
	AND CAST(SUB."FechaIngresoRegistro" AS DATE) != SUB."FechaLiquidacion"

UNION ALL

-- ============================================
-- Lee Documentos Liquidados
-- ============================================
SELECT DISTINCT 
    TO_BIGINT(:IdTipoCentralizacion)          AS "IdTipoCentralizacion",
    SUB."IdComprobanteFormaDePago" * -1       AS "IdMovimiento",
    CASE SUB."IdTipoMovimiento"
        WHEN 'MOVING' THEN 
            CASE 
                WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO_MONTO_INGRESO'
                ELSE 'DOCUMENTO_MONTO_TRASPASO_INGRESO'
            END
        ELSE 
            CASE 
                WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO_MONTO_EGRESO'
                ELSE 'DOCUMENTO_MONTO_TRASPASO_EGRESO'
            END
    END                                        AS "IdEvento", 
    CASE SUB."IdTipoMovimiento"
        WHEN 'MOVING' THEN 
            CASE 
                WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO MONTO INGRESO'
                ELSE 'DOCUMENTO MONTO TRASPASO INGRESO'
            END
        ELSE 
            CASE 
                WHEN SUB."ComprobanteNeteado" = 'N' THEN 'DOCUMENTO MONTO EGRESO'
                ELSE 'DOCUMENTO MONTO TRASPASO EGRESO'
            END
    END                                        AS "Nombre", 
    SUB."Monto"                                AS "MontoEvento", 
    SUB."IdMoneda"                             AS "CodigoMoneda"
FROM (
    SELECT
        Comprobante."IdEstado",
        ComprobanteFormaDePago."FechaIngresoRegistro",
        Comprobante."IdEmpresa",
        ComprobanteFormaDePago."IdComprobanteFormaDePago",
        Comprobante."IdTipoMovimiento",
        ComprobanteFormaDePago."Monto",
        Comprobante."IdMoneda",
        Comprobante."ComprobanteNeteado",
        CAST(
            CASE 
                WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ'
                    THEN ADD_DAYS(
                        TO_DATE(Comprobante."FechaLiquidacion", 'YYYY-MM-DD'),
                        ComprobanteFormaDePago."DiasRetencion"
                    )
                ELSE ComprobanteFormaDePago."FechaHoraLiquidacion"
            END AS DATE
        ) AS "FechaLiquidacion"
    FROM "BSCL.Tesoreria.BD::TES.Comprobante"             AS Comprobante
    INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago
        ON  ComprobanteFormaDePago."IdComprobante" = Comprobante."IdComprobante"
        AND (
                (ComprobanteFormaDePago."IdEstado" = 'LIQ' AND :LocSistema = 'CL')
                OR
                (ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')
            )
    WHERE Comprobante."IdConcepto" NOT IN ('PAREGR', 'PARING')
		AND CAST(Comprobante."FechaLiquidacion" AS DATE) = :FechaContable
) SUB
WHERE SUB."IdEmpresa" = :IdEmpresa
	AND (
        (SUB."IdEstado" = 'LIQ' AND :LocSistema = 'CL')
        OR
        (SUB."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')
      )
      
UNION ALL

-- ====================================================================================================
-- Lee Comprobantes solicitudes de ingreso y egreso liquidados SOLO COMPRAS Y VENTAS SPOT a TC/CIERRE
-- ====================================================================================================
SELECT 
	TO_BIGINT(:IdTipoCentralizacion)                       			AS "IdTipoCentralizacion",
    ComprobanteFormaDePago."IdComprobanteFormaDePago"	    		AS "IdMovimiento",
    CASE Comprobante."IdTipoMovimiento" 
    	WHEN 'MOVING' THEN 'SOLICITUD_MONTO_INGRESO_TC_CIERRE'
    	ELSE 'SOLICITUD_MONTO_EGRESO_TC_CIERRE'
    END                                                   		 	AS "IdEvento", 
    CASE Comprobante."IdTipoMovimiento"
		WHEN 'MOVING' THEN 'SOLICITUD MONTO INGRESO T/C CIERRE'
		ELSE 'SOLICITUD MONTO EGRESO T/C CIERRE'
    END                                                    			AS "Nombre", 
	FACT."MontoLiquidacion" 										AS "MontoEvento", 
    Comprobante."IdMoneda"                                 			AS "CodigoMoneda"
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ConsultarComprobanteDetalle
	ON  ConsultarComprobanteDetalle."IdComprobante" = Comprobante."IdComprobante"
    AND ConsultarComprobanteDetalle."IdEstado" IN ('LIQ', 'ING')
    AND ConsultarComprobanteDetalle."IdTipoOperacion"  IN ('COMSPT','VENSPT')
INNER JOIN  "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ComprobanteFormaDePago 
	ON ComprobanteFormaDePago."IdComprobante" = Comprobante."IdComprobante"
	AND ((ComprobanteFormaDePago."IdEstado" IN ('LIQ','CRT') AND :LocSistema = 'CL') OR (ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE'))	
  	AND Comprobante."IdConcepto"  NOT IN ('PAREGR','PARING')
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" FACT 
	ON (ConsultarComprobanteDetalle."IdComprobanteOrigen" = FACT."IdComprobante")	
WHERE Comprobante."IdEmpresa" = :IdEmpresa
	AND (
        (Comprobante."IdEstado" = 'LIQ' AND :LocSistema = 'CL') 
        OR 
        (Comprobante."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')
      )
	AND Comprobante."IdConcepto" NOT IN ('PAREGR', 'PARING')
	AND Comprobante."ComprobanteNeteado" = 'N'
	AND Comprobante."FechaLiquidacion" = :FechaContable
 
UNION ALL
 
-- ================================================================================
-- Lee Documentos Certificados con valuta SOLO COMPRAS Y VENTAS SPOT a TC/CIERRE
-- ================================================================================
SELECT 
    TO_BIGINT(:IdTipoCentralizacion)          			AS "IdTipoCentralizacion",
    SUB."IdComprobanteFormaDePago"*-1       			AS "IdMovimiento",
    CASE SUB."IdTipoMovimiento"
        WHEN 'MOVING' THEN 'DOCUMENTO_MONTO_INGRESO_TC_CIERRE'
		ELSE 'DOCUMENTO_MONTO_EGRESO_TC_CIERRE'
	END 												AS "IdEvento", 
    CASE SUB."IdTipoMovimiento"
        WHEN 'MOVING' THEN 'DOCUMENTO MONTO INGRESO T/C CIERRE'
		ELSE 'DOCUMENTO MONTO EGRESO T/C CIERRE'
    END                                        			AS "Nombre", 
    SUB."MontoLiquidacion" 									AS "MontoEvento", 
    SUB."IdMoneda"                             			AS "CodigoMoneda"
FROM (
    SELECT
        Comprobante."IdEstado",
        ComprobanteFormaDePago."FechaIngresoRegistro",
        Comprobante."IdEmpresa",
        ComprobanteFormaDePago."IdComprobanteFormaDePago",
        Comprobante."IdTipoMovimiento",
        ComprobanteFormaDePago."Monto",
        Comprobante."IdMoneda",
        Comprobante."ComprobanteNeteado",
        FACT."MontoLiquidacion",
        CAST(
            CASE 
                WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ' 
                    THEN ADD_DAYS(
                        TO_DATE(Comprobante."FechaLiquidacion", 'YYYY-MM-DD'),
                        ComprobanteFormaDePago."DiasRetencion"
                    )
                ELSE ComprobanteFormaDePago."FechaHoraLiquidacion"
            END AS DATE
        ) AS "FechaLiquidacion"
    FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante
	INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ConsultarComprobanteDetalle
		ON  ConsultarComprobanteDetalle."IdComprobante" = Comprobante."IdComprobante"
    	AND ConsultarComprobanteDetalle."IdEstado" IN ('LIQ', 'ING')
    	AND ConsultarComprobanteDetalle."IdTipoOperacion"  IN ('COMSPT','VENSPT')
    INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago
		ON  ComprobanteFormaDePago."IdComprobante" = Comprobante."IdComprobante"
        AND ComprobanteFormaDePago."IdEstado" IN ('CRT')
	INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" FACT 
		ON (ConsultarComprobanteDetalle."IdComprobanteOrigen" = FACT."IdComprobante")	    
    WHERE Comprobante."IdConcepto" NOT IN ('PAREGR', 'PARING')
    	AND Comprobante."ComprobanteNeteado" = 'N'
) SUB
WHERE SUB."IdEmpresa" = :IdEmpresa
	AND (
        (SUB."IdEstado" = 'LIQ' AND :LocSistema = 'CL')
        OR
        (SUB."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')
      )
  	AND CAST(SUB."FechaIngresoRegistro" AS DATE) = :FechaContable
  	AND CAST(SUB."FechaIngresoRegistro" AS DATE) != SUB."FechaLiquidacion"

UNION ALL

-- ====================================================================
-- Lee Documentos Liquidados SOLO COMPRAS Y VENTAS SPOT a TC/CIERRE
-- ====================================================================
SELECT DISTINCT 
    TO_BIGINT(:IdTipoCentralizacion)          	AS "IdTipoCentralizacion",
    SUB."IdComprobanteFormaDePago"*-1         	AS "IdMovimiento",
    CASE SUB."IdTipoMovimiento"
        WHEN 'MOVING' THEN 'DOCUMENTO_MONTO_INGRESO_TC_CIERRE'
        ELSE 'DOCUMENTO_MONTO_EGRESO_TC_CIERRE'
    END                                        	AS "IdEvento", 
    CASE SUB."IdTipoMovimiento"
        WHEN 'MOVING' THEN 'DOCUMENTO MONTO INGRESO T/C CIERRE'
        ELSE 'DOCUMENTO MONTO EGRESO T/C CIERRE'
    END                                        	AS "Nombre", 
    SUB."MontoLiquidacion"  					AS "MontoEvento", 
    SUB."IdMoneda"                             	AS "CodigoMoneda"
FROM (
    SELECT
        Comprobante."IdEstado",
        ComprobanteFormaDePago."FechaIngresoRegistro",
        Comprobante."IdEmpresa",
        ComprobanteFormaDePago."IdComprobanteFormaDePago",
        Comprobante."IdTipoMovimiento",
        ComprobanteFormaDePago."Monto",
        Comprobante."IdMoneda",
        Comprobante."ComprobanteNeteado",
        FACT."MontoLiquidacion",
        CAST(
            CASE 
                WHEN ComprobanteFormaDePago."IdEstado" <> 'LIQ'
                    THEN ADD_DAYS(
                        TO_DATE(Comprobante."FechaLiquidacion", 'YYYY-MM-DD'),
                        ComprobanteFormaDePago."DiasRetencion"
                    )
                ELSE ComprobanteFormaDePago."FechaHoraLiquidacion"
            END AS DATE
        ) AS "FechaLiquidacion"
    FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS Comprobante
    INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS ConsultarComprobanteDetalle
    	ON  ConsultarComprobanteDetalle."IdComprobante" = Comprobante."IdComprobante"
    	AND ConsultarComprobanteDetalle."IdEstado" IN ('LIQ', 'ING')
    	AND ConsultarComprobanteDetalle."IdTipoOperacion" IN ('COMSPT', 'VENSPT')
    INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS ComprobanteFormaDePago
        ON  ComprobanteFormaDePago."IdComprobante" = Comprobante."IdComprobante"
        AND (
                (ComprobanteFormaDePago."IdEstado" = 'LIQ' AND :LocSistema = 'CL')
                OR
                (ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')
            )
	INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" FACT 
		ON (ConsultarComprobanteDetalle."IdComprobanteOrigen" = FACT."IdComprobante")	    
    WHERE Comprobante."IdConcepto" NOT IN ('PAREGR', 'PARING')
    	AND Comprobante."ComprobanteNeteado" = 'N'
     	AND CAST(Comprobante."FechaLiquidacion" AS DATE) = :FechaContable  
) SUB
WHERE SUB."IdEmpresa" = :IdEmpresa
	AND (
        (SUB."IdEstado" = 'LIQ' AND :LocSistema = 'CL')
        OR
        (SUB."IdEstado" IN ('LIQ','ANU') AND :LocSistema = 'PE')
	);
      