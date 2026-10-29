DELETE FROM "BSCL.APV.BD::REC.Cliente" AS "CLR"
WHERE (:IdRecaudacionEmpleador = 0 OR "CLR"."IdRecaudacionEmpleador" = :IdRecaudacionEmpleador)
	AND (:IdRecaudacionCliente = 0 OR "CLR"."IdRecaudacionCliente" = :IdRecaudacionCliente)
	AND "CLR"."IdRecaudacion" =  :IdRecaudacion
	AND "CLR"."IdEmpresa"	  =  :IdEmpresa;