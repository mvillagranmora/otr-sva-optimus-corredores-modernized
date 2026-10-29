SELECT 
	REC."IdRecaudacion"								--BIGINT 			NOT NULL,
	,REC."IdEmpresa"									--BIGINT			NOT NULL,
	,REC."CodigoArchivo"								--NVARCHAR(10),
	,REC."FolioCartola"								--BIGINT,
	,REC."LineaCartola"								--BIGINT,
	,REC."Sucursal"									--NVARCHAR(200)	NOT NULL,
	,TO_VARCHAR(REC."FechaDeposito", 'YYYY/MM/DD') AS "FechaDeposito"	--DATE 			NOT NULL,
	,REC."CuentaCorriente"							--BIGINT			NOT NULL,
	,REC."MontoDeposito"							--DECIMAL(26,8)	NOT NULL,
	,REC."ConceptoRecaudacion"						--NVARCHAR(300),
	,REC."Origen"									--NVARCHAR(100),
	,REC."Observacion"								--NVARCHAR(200),
	,REC."NumeroDocumento"							--BIGINT			NOT NULL,
	,REC."IdTipoDeposito"							--NVARCHAR(6)		NOT NULL,
	,REC."IdBanco"									--BIGINT			NOT NULL,
	,REC."IdRegimenTributario"						--BIGINT,
	,REC."IdCarga"									--BIGINT			NOT NULL,
	,REC."IdEstado"									--NVARCHAR(3)		NOT NULL
FROM "BSCL.APV.BD::REC.Recaudacion" AS REC
INNER JOIN "BSCL.APV.BD::APV.CargaMasiva" AS CM ON CM."IdCargaMasiva" = REC."IdCarga"
WHERE CM."NombreCargaArchivo" = :NombreCargaArchivo;