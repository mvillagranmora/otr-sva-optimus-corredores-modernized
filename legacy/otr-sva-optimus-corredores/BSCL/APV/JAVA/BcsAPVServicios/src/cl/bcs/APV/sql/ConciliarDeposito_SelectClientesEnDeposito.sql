SELECT 
	"IdRecaudacion"							--BIGINT,
	,"IdRecaudacionCliente"
	,"IdCliente"							--BIGINT			NOT NULL,
	,"IdRegimenTriburario"					--NVARCHAR(50)	NOT NULL,
	,"IdEstado"								--NVARCHAR(3)		NOT NULL,
FROM "BSCL.APV.BD::REC.Cliente"
WHERE "IdRecaudacion" = :IdRecaudacion;
