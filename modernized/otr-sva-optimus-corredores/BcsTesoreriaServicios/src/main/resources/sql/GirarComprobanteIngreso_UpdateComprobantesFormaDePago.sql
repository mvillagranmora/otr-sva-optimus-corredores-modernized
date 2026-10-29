UPDATE CFP
		SET
			  "IdCuentaBancariaCorredora" 			= :IdCuentaBancaria
			 ,"IdTipoCuentaBancariaCorredora"  	    = :IdTipoCuentaBancaria
			 ,"NombreTipoCuentaBancariaCorredora"   = :NombreTipoCuentaBancaria
			 ,"NombreCuentaBancariaCorredora" 		= :NombreCuentaBancaria
			 ,"NumeroCuentaBancariaCorredora"   	= :NumeroCuentaBancaria
		FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar" AS GIR ON (CFP."IdComprobante" = GIR."IdComprobante")
AND CFP."IdMedioDePago" = CAST(:IdMedioDePago AS VARCHAR(3))    
AND CFP."IdEstado" <> 'ANU';
        