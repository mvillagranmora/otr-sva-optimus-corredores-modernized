SELECT 
	REC."IdRecaudacion",
	RC."IdRecaudacionCliente",
	RC."IdEstado",
	RC."IdCliente",
	RC."IdRegimenTriburario" AS "IdRegimenTributario",
	CASE 
		WHEN RE."IdRecaudacionEmpleador" > 0 THEN 'ATREMP' 
		WHEN RC."IdRecaudacionCliente"   > 0 THEN 'DIREMP'
		ELSE '' 
	END AS "DetalleRecaudacion"
FROM "BSCL.APV.BD::REC.Recaudacion" REC
LEFT JOIN "BSCL.APV.BD::REC.Cliente" RC ON RC."IdRecaudacion" = REC."IdRecaudacion"
LEFT JOIN "BSCL.APV.BD::REC.Empleador" RE ON RE."IdRecaudacionEmpleador" = RC."IdRecaudacionEmpleador"
WHERE REC."IdRecaudacion" IN (:IdsRecaudacion);