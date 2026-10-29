-- Repasar Global de formas de pago comprobante
UPDATE CFP
   SET 	 "DiasRetencion"	 	= CASE WHEN TMMP."AfectoADiasRetencion" = 'S' THEN CFP."DiasRetencion" ELSE 0 END			   
   		,"FechaAfectaSaldo"		= CASE WHEN TMMP."AfectoADiasRetencion" = 'S' AND instr(TMMP."AfectaSaldoEnEstado",'ING')> 0 AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0
									   THEN ADD_DAYS(TO_DATE(:FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')												   
									   WHEN TMMP."AfectoADiasRetencion" = 'N' AND instr(TMMP."AfectaSaldoEnEstado",'ING')> 0 AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0
									   THEN TO_DATE(:FechaMovimiento, 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
						               ELSE '0001-01-01'
								   END
  FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_ComprobanteFormaDePago" as CFP
 INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" AS TMMP 
	 	 ON TMMP."IdTipoMovimiento" = CFP."IdTipoMovimiento"
    	AND TMMP."IdMedioDePago" 	= CFP."IdMedioDePago";