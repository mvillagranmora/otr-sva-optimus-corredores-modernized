SELECT 	
     EMP."IdRecaudacionEmpleador" AS "IdEmpleador"
	,EMP."IdRecaudacion"
	,EMP."PeriodoPago"
FROM "BSCL.APV.BD::REC.Empleador" AS EMP
WHERE "IdRecaudacionEmpleador" IN (:IdRecaudacionEmpleador)