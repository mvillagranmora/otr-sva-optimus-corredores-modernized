SELECT	*
FROM	(
			SELECT 	LEFT('S80080' || '          ', 10)  AS "CodigoAI" 
					,:p_FechaDesde AS "FechaInformacion"
					,'S' AS "FlagInscrito"					
					,LEFT(TRIM('ALICORC1') || '                    ', 20) AS "CodigoIF"
					,RIGHT('00000000000000' || TRIM('422600021'), 14)  AS "CantidadNominal"
					,RIGHT('0000000000000000' || TRIM('0000042100000000'), 16)  AS "Precio"					
					,RIGHT('0000000' || TRIM('1585'), 7)  AS "FactorPonderacion"										
			FROM	DUMMY UNION ALL 
			SELECT 	LEFT('S8540080' || '          ', 10)  AS "CodigoAI" 
					,:p_FechaDesde AS "FechaInformacion"
					,'S' AS "FlagInscrito"					
					,LEFT(TRIM('BAP') || '                    ', 20) AS "CodigoIF"
					,RIGHT('00000000000000' || TRIM('87700021'), 14)  AS "CantidadNominal"
					,RIGHT('0000000000000000' || TRIM('0000077100000000'), 16)  AS "Precio"					
					,RIGHT('0000000' || TRIM('1585'), 7)  AS "FactorPonderacion"										
			FROM	DUMMY UNION ALL 
			SELECT 	LEFT('S800320' || '          ', 10)  AS "CodigoAI" 
					,:p_FechaDesde AS "FechaInformacion"
					,'S' AS "FlagInscrito"					
					,LEFT(TRIM('BVN') || '                    ', 20) AS "CodigoIF"
					,RIGHT('00000000000000' || TRIM('44600021'), 14)  AS "CantidadNominal"
					,RIGHT('0000000000000000' || TRIM('0000078100000000'), 16)  AS "Precio"					
					,RIGHT('0000000' || TRIM('553'), 7)  AS "FactorPonderacion"										
			FROM	DUMMY 
		)
