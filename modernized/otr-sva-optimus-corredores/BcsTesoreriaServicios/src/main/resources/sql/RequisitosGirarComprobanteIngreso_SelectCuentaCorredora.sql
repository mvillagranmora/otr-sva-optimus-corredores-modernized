SELECT 	1 AS "Prioridad",	
				CASE 
					WHEN :p_simularOperacion = 'N' AND (COUNT(CTA."IdCuentaBanco") = 0) THEN 0
					WHEN :p_simularOperacion = 'N' AND (COALESCE(PER."IdPersona",0) = 0) THEN 0 
					ELSE 1 END AS "Cumple",
				CASE 
					WHEN :p_simularOperacion = 'N' AND (COUNT(CTA."IdCuentaBanco") = 0) THEN 'CUENTA_CORRIENTE_NO_EXISTE'
					WHEN :p_simularOperacion = 'N' AND (COALESCE(PER."IdPersona",0) = 0) THEN 'BANCO_CUENTA_CORRIENTE_SIN_CONFIGURACION'
					ELSE '' 
				END AS "CodigoError"
				,'' AS "CodigoDato"
		       	,'' AS "ValorDato"
		       	,0 AS "IdComprobante"
		FROM "BSCL.Tesoreria.BD::TES.CuentaBanco" AS CTA
		INNER JOIN "BSCL.Plataforma.BD::BAN.Banco" AS BAN ON (BAN."IdBanco" = CTA."IdBanco")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = BAN."IdPersona")
		WHERE CTA."IdCuentaBanco" = :p_IdCuentaBancariaCorredora
		GROUP BY CTA."IdCuentaBanco", PER."IdPersona"