SELECT 	2 AS "Prioridad",	
				CASE 
					WHEN  (COUNT(CTA."IdCuentaBancaria") = 0) THEN 0
					WHEN  (COALESCE(PER."IdPersona",0) = 0) THEN 0 
					ELSE 1 END AS "Cumple",
				CASE 
					WHEN  (COUNT(CTA."IdCuentaBancaria") = 0) THEN 'CUENTA_CORRIENTE_SOCIO_NO_EXISTE'
					WHEN  (COALESCE(PER."IdPersona",0) = 0) THEN 'BANCO_CLIENTE_NO_XISTE'
					ELSE '' 
				END AS "CodigoError"
				,'IdComprobante' AS "CodigoDato"
		       	,CAST(GIR."IdComprobante" as NVARCHAR(50)) AS "ValorDato"
		       	,GIR."IdComprobante" AS "IdComprobante"
		FROM #RequisitosGirarComprobanteEgreso_p_ComprobantesAProcesar GIR  
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobante" Cmbte ON  GIR."IdComprobante" = Cmbte."IdComprobante"
		left JOIN BSCL."BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CTA ON   Cmbte."NumeroCuentaBancariaSocio" = CTA."NumeroCuentaBancaria" AND Cmbte."IdPersona" = CTA."IdPersona"  
		LEFT  JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON  (BAN."IdBanco" = CMBTE."IdBancoSocio")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = BAN."IdPersona")
		GROUP BY CTA."IdCuentaBancaria", PER."IdPersona", GIR."IdComprobante"