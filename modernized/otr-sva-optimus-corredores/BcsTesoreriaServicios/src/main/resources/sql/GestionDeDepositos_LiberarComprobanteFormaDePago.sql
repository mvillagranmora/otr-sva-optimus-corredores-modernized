
			
-- Al anular el deposito se libera la forma de pago del comprobante asociada a ese deposito		

UPDATE 	CFP
   SET	CFP."IdEstado" 		= CASE WHEN instr("EstadosPosibleTipoMovimientoMedioDePago",'CRT')>0 THEN 'CRT' ELSE 'ING' END,
   		CFP."IdDeposito"  	= 0,
   		CFP."Depositado"  	= NULL
   		
FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle" as DD
											 
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = DD."IdComprobanteFormaDePago"
INNER JOIN "BSCL.Tesoreria.BD::TES.TipoMovimientoMedioDePago" as TMMP ON TMMP."IdMedioDePago" = CFP."IdMedioDePago" AND TMMP."IdTipoMovimiento" = CFP."IdTipoMovimiento"

WHERE :Id = CASE 
				WHEN (:Entidad = 'TES.Deposito') THEN DD."IdDeposito"
 				ELSE DD."IdDepositoDetalle"
 			END;			

