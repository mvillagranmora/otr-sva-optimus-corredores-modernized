SELECT REC."IdRecaudacion"
FROM "BSCL.APV.BD::REC.Recaudacion" REC
INNER JOIN "BSCL.APV.BD::REC.Cliente" AS C
	ON REC."IdRecaudacion"	 =  C."IdRecaudacion"
WHERE (:IdRecaudacion = 0 OR REC."IdRecaudacion" IN (:IdsRecaudacion)) AND C."IdEstado" IN ('CON','REZ')
FOR UPDATE;