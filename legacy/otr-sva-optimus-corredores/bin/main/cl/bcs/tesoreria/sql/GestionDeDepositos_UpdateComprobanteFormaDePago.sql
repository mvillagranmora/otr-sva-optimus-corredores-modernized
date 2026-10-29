

UPDATE CFP
SET  "Depositado"		= 'S',
   	 "FechaAfectaSaldo"	= CASE WHEN instr(TFP."AfectaSaldoEnEstado",'LIQ') > 0 
    								AND COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0 
    								AND COALESCE(CFP."FechaAfectaSaldo",'0001-01-01') = '0001-01-01' 
    							   THEN ADD_DAYS(TO_DATE(:FechaMovimiento, 'YYYY-MM-DD'), CFP."DiasRetencion") || ' ' || TO_VARCHAR (CURRENT_UTCTIMESTAMP,'HH24:MI:SS.FF7')
    							   ELSE CFP."FechaAfectaSaldo"
							   END
							   
FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD					   
	    
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP 
	    ON CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago"
INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" as TFP
		ON TFP."IdTipoMovimiento" = CFP."IdTipoMovimiento" AND TFP."IdMedioDePago" = CFP."IdMedioDePago"


WHERE  :Id = CASE WHEN (:Entidad = 'TES.Deposito') THEN DD."IdDeposito" ELSE DD."IdDepositoDetalle" END
		
AND DD."IdEstado" = 'LIQ'
AND CFP."IdEstado" = 'LIQ';
			   	  