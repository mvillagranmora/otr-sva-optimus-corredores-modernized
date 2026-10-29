DELETE FROM "BSCL.Custodia.BD::CUS.CuadraturaCircular1518" 
WHERE "FechaCuadratura" = :FechaCuadratura
OR "FechaCuadratura" IN  (
							SELECT tt."FechaCuadratura"
							FROM
							(
								SELECT
									ROW_NUMBER() OVER(PARTITION BY MONTH("FechaCuadratura") ORDER BY "FechaCuadratura" DESC) AS "IdTemporal"
									,"FechaCuadratura"																					
								FROM "BSCL.Custodia.BD::CUS.CuadraturaCircular1518" 
								WHERE YEAR("FechaCuadratura") <= YEAR(TO_VARCHAR(ADD_MONTHS(:FechaCuadratura, -2))) AND  MONTH ("FechaCuadratura") <= MONTH (TO_VARCHAR(ADD_MONTHS(:FechaCuadratura, -2)))  
								GROUP BY  "FechaCuadratura" 
							) AS tt
							WHERE tt."IdTemporal" != 1
						);