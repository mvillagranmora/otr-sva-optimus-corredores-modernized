UPDATE CFP
		SET
		   	  "IdUsuarioGiro"        = :IdUsuario  
			 ,"FechaGiro"         	 = :FechaGiro
		 	 ,"IdEstado"          	 = 'LIQ'
		 	 ,"IdComprobantePartida" = :IdPAREGR
		FROM  "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
		INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = CFP."IdComprobante")
		INNER JOIN "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar" AS GIR ON (COM."IdComprobante" = GIR."IdComprobante")
		WHERE CFP."IdMedioDePago" = :IdMedioDePago
		AND CFP."IdEstado" NOT IN ('ANU','MOD');