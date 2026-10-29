SELECT	*
FROM	(
			SELECT 	LEFT('S80080' || '         ', 10) AS "CodigoAI" 
					,:p_FechaDesde AS "FechaInformacion"
					,RIGHT('000000000000' || TRIM(COALESCE(317681, 0)), 12) AS "MontoDescubierto"
					,'1' AS "TipoCodigoCliente"
					,RIGHT('00000000' || TRIM('42261021'), 8) AS "CodigoCliente"
			FROM	DUMMY UNION ALL 
			SELECT 	LEFT('S80080' || '         ', 10) AS "CodigoAI"  
					,:p_FechaDesde AS "FechaInformacion"
					,RIGHT('000000000000' || TRIM(COALESCE(7768231, 0)), 12) AS "MontoDescubierto"
					,'1' AS "TipoCodigoCliente"
					,RIGHT('00000000' || TRIM('471021'), 8) AS "CodigoCliente"
			FROM	DUMMY UNION ALL 
			SELECT 	LEFT('S80080' || '         ', 10) AS "CodigoAI" 
					,:p_FechaDesde AS "FechaInformacion"
					,RIGHT('000000000000' || TRIM(COALESCE(974315, 0)), 12) AS "MontoDescubierto"
					,'1' AS "TipoCodigoCliente"
					,RIGHT('00000000' || TRIM('785021'), 8) AS "CodigoCliente"
			FROM	DUMMY 
		)
