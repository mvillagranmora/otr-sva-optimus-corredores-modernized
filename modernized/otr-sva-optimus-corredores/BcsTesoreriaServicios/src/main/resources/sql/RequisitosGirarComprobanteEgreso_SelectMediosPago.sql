
SELECT "IdMedioDePago", SUM("MontoPorMedioDePago") AS "MontoPorMedioDePago"
						FROM (
						SELECT
							  COM."IdComprobante"
							 ,CFP."IdMedioDePago"
							 ,SUM(CFP."Monto") AS "MontoPorMedioDePago"
						FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar AS GIR					 
						INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" COM ON (COM."IdComprobante" = GIR."IdComprobante")
						INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP ON (CFP."IdComprobante" = COM."IdComprobante")  -- toma sólo comprobantes con forma de pago asignada
						WHERE (CFP."IdEstado" <> 'MOD' AND CFP."IdEstado" <> 'ANU' AND CFP."IdComprobantePartida" IS NULL)
						GROUP BY COM."IdComprobante", CFP."IdMedioDePago"
						)
GROUP BY "IdMedioDePago"
HAVING COUNT("IdMedioDePago") = (SELECT COUNT("IdComprobante")
				FROM 
				(
						SELECT COM."IdComprobante"
						FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar AS GIR
						INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" COM ON (COM."IdComprobante" = GIR."IdComprobante")
						INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP ON (CFP."IdComprobante" = COM."IdComprobante")  -- usa INNER para que tome solo comprobantes con forma de pago asignada
						WHERE (CFP."IdEstado" <> 'MOD' AND CFP."IdEstado" <> 'ANU' AND CFP."IdComprobantePartida" IS NULL)
						GROUP BY COM."IdComprobante"		
				));
