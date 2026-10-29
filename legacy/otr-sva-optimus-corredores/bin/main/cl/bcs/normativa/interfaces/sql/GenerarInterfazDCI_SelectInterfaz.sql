SELECT	*
FROM	(
			SELECT 	LEFT('S80080' || '         ', 10) AS "CodigoAI" 
					,:p_FechaDesde AS "FechaInformacion"
					,'203' AS "CodigoCuenta"
					,'0' AS "Signo"
					,RIGHT('000000000000' || TRIM('42261021'), 12) AS "Monto"
					,'1' AS "TipoInformacion"
			FROM	DUMMY UNION ALL 
			SELECT 	LEFT('S82280' || '         ', 10) AS "CodigoAI" 
					,:p_FechaDesde AS "FechaInformacion"
					,'707' AS "CodigoCuenta"
					,'0' AS "Signo"
					,RIGHT('000000000000' || TRIM('5778021'), 12) AS "Monto"
					,'1' AS "TipoInformacion"
			FROM	DUMMY UNION ALL 
			SELECT 	LEFT('S83080' || '         ', 10) AS "CodigoAI" 
					,:p_FechaDesde AS "FechaInformacion"
					,'806' AS "CodigoCuenta"
					,'1' AS "Signo"
					,RIGHT('000000000000' || TRIM('91022'), 12) AS "Monto"
					,'2' AS "TipoInformacion"
			FROM	DUMMY UNION ALL 
			SELECT 	LEFT('S80380' || '         ', 10) AS "CodigoAI" 
					,:p_FechaDesde AS "FechaInformacion"
					,'203' AS "CodigoCuenta"
					,'0' AS "Signo"
					,RIGHT('000000000000' || TRIM('198021'), 12) AS "Monto"
					,'1' AS "TipoInformacion"
			FROM	DUMMY 
		)
