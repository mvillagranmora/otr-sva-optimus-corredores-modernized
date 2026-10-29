DELETE FROM "BSCL.Custodia.BD::CUS.CuadraturaCircular1720" 
WHERE "FechaCuadratura" = :FechaCuadratura
OR "FechaCuadratura" <=  (SELECT tt2."FechaCuadratura" 
								FROM(
									SELECT TOP 1 tt."FechaCuadratura"
										FROM(
											SELECT
												ROW_NUMBER() OVER(PARTITION BY MONTH("FechaCuadratura") ORDER BY "FechaCuadratura" DESC) 			AS "IdTemporal"
												,"FechaCuadratura"																					
											FROM "BSCL.Custodia.BD::CUS.CuadraturaCircular1720" 
											WHERE YEAR("FechaCuadratura") = YEAR(TO_VARCHAR(ADD_MONTHS(:FechaCuadratura ,-1))) AND  MONTH ("FechaCuadratura") = MONTH (TO_VARCHAR(ADD_MONTHS(:FechaCuadratura, -1)))  
											GROUP BY  "FechaCuadratura" 
											) AS tt
										WHERE tt."IdTemporal" != 1) AS tt2);	
