UPDATE "BSCL.APV.BD::REC.Cliente"
SET 
	    "IdEstado"				= :IdEstado
WHERE	"IdRecaudacionCliente" 	= :IdRecaudacionCliente
AND 	"IdEmpresa"				= :IdEmpresa;