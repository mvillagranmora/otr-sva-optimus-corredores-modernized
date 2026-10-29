				INSERT INTO "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
				(
					  "IdNominaTransferenciaDetalle"
					 ,"IdNominaTransferencia"
					 ,"IdComprobanteFormaDePago"
					 ,"IdUsuario"
					 ,"IdEstado"
				)
				SELECT 
					 "BSCL.Tesoreria.BD.Secuencias::NominaTransferenciaDetalle".NEXTVAL
					 ,:v_IdNominaTransferencia
					 ,CFP."IdComprobanteFormaDePago"
					 ,:p_IdUsuario
					 ,'LIQ'
				FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_ComprobanteGenerar" AS GIR
				INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = GIR."IdComprobante")
				INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobante" = COM."IdComprobante")
				WHERE CFP."IdMedioDePago" = :p_IdMedioDePago
				AND   CFP."IdEstado"      IN ('LIQ');