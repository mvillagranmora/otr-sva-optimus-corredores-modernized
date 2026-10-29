SELECT 
	EMP."IdRecaudacionEmpleador"									--BIGINT 			NOT NULL,
	,EMP."IdRecaudacion"											--BIGINT,
	,EMP."CodigoArchivo"											--NVARCHAR(10),
	,EMP."IdEmpresa"												--BIGINT			NOT NULL,
	,EMP."IdPersona"												--BIGINT			NOT NULL,
	,TO_VARCHAR(EMP."FechaIngreso",'YYYY/MM/DD') AS "FechaIngreso"	--DATE 				NOT NULL,
	,EMP."PeriodoPago"												--NVARCHAR(7)		NOT NULL,
	,EMP."NumeroHojas"												--BIGINT			NOT NULL,
	,EMP."TipoAfiliado"												--NVARCHAR(3),
	,EMP."TipoPago"													--NVARCHAR(50)		NOT NULL,
	,EMP."Insistencias"												--NVARCHAR(50),
	,EMP."IdBanco"													--BIGINT,
	,EMP."NumeroDocumento"											--BIGINT,
	,EMP."MontoDepositoConvenido"									--DECIMAL(26,8)		NOT NULL,
	,EMP."MontoDepositoAPV"											--DECIMAL(26,8)		NOT NULL,
	,EMP."Reajustes"												--DECIMAL(26,8)		NOT NULL,
	,EMP."Intereses"												--DECIMAL(26,8)		NOT NULL,
	,EMP."Origen"													--NVARCHAR(100)		NOT NULL,
	,EMP."Observaciones"											--NVARCHAR(200),
	,EMP."IdEstado"													--NVARCHAR(3)		NOT NULL,
	,COALESCE(DOC."DocumentoIdentidad", '') AS "DocumentoIdentidad"
FROM "BSCL.APV.BD::REC.Empleador" AS EMP
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS  DOC ON DOC."IdPersona" = EMP."IdPersona" AND DOC."Predeterminado" = 'S'
WHERE "IdRecaudacion" = :p_IdRecaudacion;