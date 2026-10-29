UPDATE 	CFP
		   SET	CFP."IdEstado" = 'ANU'
		  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP
		 INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdComprobantePartidaEgreso" = CFP."IdComprobantePartida"
		 INNER JOIN #AnularNominasTransferencias_p_RegistrosAProcesar as RP ON RP."Id" = NT."IdNominaTransferencia";
