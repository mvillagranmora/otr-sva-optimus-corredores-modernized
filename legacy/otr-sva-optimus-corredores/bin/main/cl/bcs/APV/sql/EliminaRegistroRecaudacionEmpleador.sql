DELETE FROM "BSCL.APV.BD::REC.Empleador" AS "EMP"
WHERE "EMP"."IdRecaudacionEmpleador" = :IdRecaudacionEmpleador
	AND "EMP"."IdRecaudacion" =	:IdRecaudacion
	AND "EMP"."IdEmpresa"	  = :IdEmpresa;