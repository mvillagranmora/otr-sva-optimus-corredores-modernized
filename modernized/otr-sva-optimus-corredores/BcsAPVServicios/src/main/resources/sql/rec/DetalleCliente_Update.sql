UPDATE "BSCL.APV.BD::REC.Cliente"
SET 
	 "IdCliente"				= :IdCliente
	,"FechaIngreso"				= :FechaIngreso
	,"TipoAfiliado"				= :TipoAfiliado
	,"IdRegimenTriburario"		= :IdRegimenTriburario
	,"MontoDepositoConvenido"	= :MontoDepositoConvenido
	,"MontoDepositoAPV"			= :MontoDepositoAPV
	,"Origen"					= :Origen
	,"IdEstado"					= :IdEstado
WHERE	"IdRecaudacionCliente" 	= :IdRecaudacionCliente
AND 	"IdEmpresa"				= :IdEmpresa;