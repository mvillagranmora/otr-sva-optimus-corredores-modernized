-- ***************************************************************************************************************
-- Cartera Pacteada - Interfaz GenerarInterfazC40CarteraPacteada
-- ***************************************************************************************************************

SELECT CASE WHEN OPE."IdTipoOperacion" = 'COMPAC' THEN 'C'
            WHEN OPE."IdTipoOperacion" = 'VENPAC' THEN 'V'
            WHEN OPE."IdTipoOperacion" = 'COMTER' THEN 'C'
            WHEN OPE."IdTipoOperacion" = 'VENTER' THEN 'V'
            WHEN OPE."IdTipoOperacion" = 'COMFWD' THEN 'C'
            WHEN OPE."IdTipoOperacion" = 'VENFWD' THEN 'V'
            ELSE '' 
            END AS TIPO_COMPROM,
        COALESCE (TO_VARCHAR(OPE."FechaOperacion", 'DD-MM-YYYY'),'')     AS FEC_OPERAC,
        COALESCE (TO_VARCHAR(OPE."FechaLiquidacion" , 'DD-MM-YYYY'),'')  AS FEC_LIQUID,        
        COALESCE (TO_VARCHAR(OPE."FechaVencimiento" , 'DD-MM-YYYY'),'')  AS FEC_VENC,
        CAST(DAYS_BETWEEN(:FiltroFecha, OPE."FechaVencimiento") AS BIGINT)            AS PLAZO,           
        COALESCE (OPE."MontoOperacionInicial" , 0)      AS MONTO_CAPITAL,
	CASE
	  WHEN OPE."IdTipoReajuste" = 'CLP' THEN
	    ROUND(OPE."MontoOperacionFinal" / NULLIF((1 + (OPE."Tasa" / 100) * NULLIF(DAYS_BETWEEN(:FiltroFecha, OPE."FechaVencimiento"), 0) / 30), 0), 0)
	  ELSE
	    ROUND(OPE."MontoOperacionFinal" / NULLIF((1 + (OPE."Tasa" / 100) * NULLIF(DAYS_BETWEEN(:FiltroFecha, OPE."FechaVencimiento"), 0) / 360), 0), 2)
	END AS VALOR_PTE  ,
                         
        COALESCE (OPE."Tasa" , 0)               		AS TASA_COMPROM,
        OPE."IdTipoReajuste"                            AS TIPO_REAJ_COMP
FROM "BSCL.Mesa.BD::MES.Operacion" OPE 
WHERE OPE."IdEmpresa" 				= 	:IdEmpresa
AND   OPE."IdEstado" 				IN  ('VIG', 'LIQ' )
AND   OPE."IdTipoProducto" 			= 	'MERF'
AND   OPE."IdTipoOperacion"       	IN ('VENPAC','COMPAC')
AND   OPE."FechaOperacion" 			<= 	:FiltroFecha
AND   OPE."FechaVencimiento"      	>	:FiltroFecha
AND  (OPE."FechaLiquidacion"		>= 	:FiltroFecha OR OPE."FechaLiquidacion" = '0001-01-01')
ORDER BY 
  FEC_LIQUID,
  FEC_OPERAC