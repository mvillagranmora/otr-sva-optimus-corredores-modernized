UPDATE "BSCL.APV.BD::REC.Empleador"
SET 
		"IdEstado"					= :IdEstado
WHERE	"IdRecaudacionEmpleador" 	= :IdRecaudacionEmpleador
AND 	"IdEmpresa"					= :IdEmpresa;