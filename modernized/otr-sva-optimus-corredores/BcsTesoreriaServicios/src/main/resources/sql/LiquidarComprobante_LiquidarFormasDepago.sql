
------------------------------------------
-- Liquida las formas de pago
------------------------------------------

UPDATE CFP
SET "IdEstado"             	= 'LIQ',
    "IdUsuarioLiquidacion" 	= :IdUsuario,
    "FechaHoraLiquidacion"	= TO_DATE(:FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7'),
    "FechaAfectaSaldo"		= CASE WHEN instr(TFP."AfectaSaldoEnEstado",'LIQ') > 0 
    								AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0 
    								AND COALESCE(CFP."FechaAfectaSaldo",'0001-01-01') = '0001-01-01' 
    							   THEN ADD_DAYS(TO_DATE(:FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
    							   ELSE CFP."FechaAfectaSaldo"
    						   END
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
INNER JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobante" AS CAL ON CFP."IdComprobante" = CAL."IdComprobanteInformado"	
INNER JOIN "BSCL.Tesoreria.BD::GBL.LiquidarComprobante_TablaComprobanteFormaDePago" AS TFP  ON TFP."IdComprobanteInformado" = CFP."IdComprobante"  AND TFP."IdComprobanteFormaDePagoInformado" = CFP."IdComprobanteFormaDePago"
WHERE CFP."IdEstado" NOT IN ('ANU','LIQ')
AND CAL."IdComprobante" > 0;  
	    