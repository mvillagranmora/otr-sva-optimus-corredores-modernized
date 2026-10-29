SELECT 
	"IdRecaudacionCliente"
FROM "BSCL.APV.BD::REC.Cliente"
WHERE "IdEstado" != 'DIS'
AND "IdEstado" != 'DEV'
AND "IdRecaudacionEmpleador" = :IdRecaudacionEmpleador;