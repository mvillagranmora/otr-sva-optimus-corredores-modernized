SELECT 
	CLI."IdRecaudacionCliente"						--BIGINT 			NOT NULL,
	,CLI."IdRecaudacion"							--BIGINT,
	,CLI."IdRecaudacionEmpleador"					--BIGINT,
	,CLI."IdEmpresa"								--BIGINT			NOT NULL,
	,CLI."IdCliente"								--BIGINT			NOT NULL,
	,TO_VARCHAR(CLI."FechaIngreso", 'YYYY/MM/DD')	--DATE 			NOT NULL,
	,CLI."TipoAfiliado"								--NVARCHAR(3)		NOT NULL,
	,CLI."IdRegimenTriburario"						--NVARCHAR(50)	NOT NULL,
	,CLI."MontoDepositoConvenido"					--DECIMAL(26,8)	NOT NULL,
	,CLI."MontoDepositoAPV"							--DECIMAL(26,8)	NOT NULL,
	,CLI."Origen"									--NVARCHAR(100)	NOT NULL,
	,CLI."IdEstado"									--NVARCHAR(3)		NOT NULL,
	,COALESCE(DOCCLI."DocumentoIdentidad", '') AS "DocumentoIdentidad"
FROM "BSCL.APV.BD::REC.Cliente" AS CLI
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS DOCCLI ON DOCCLI."IdCliente" = CLI."IdCliente"
WHERE "IdRecaudacion" = :p_IdRecaudacion;