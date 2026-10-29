SELECT
0		AS "IdPrecioTasa",
INS."IdInstrumento",
DTA."Nemo"	AS "Nemotecnico",
INS."IdClasificacionTipoInstrumento",
INS."IdTipoInstrumento",
DTA."Tasa",
0		AS "TasaMod",
DTA."TasaEstimada",
0		AS "TasaEstimadaMod",
DTA."Plazo",
0		AS "PlazoMod",
'CAR'	AS "IdEstado",
INS."IdMoneda"
FROM "BSCL.Plataforma.BD::GBL.CargarArchivoTasas_Entrada_CargarArchivoTasas" DTA
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
	ON (DTA."Nemo" = INS."Nemotecnico")
	WHERE INS."IdInstrumento" IS NOT NULL
	GROUP BY 
		INS."IdInstrumento", 
		DTA."Nemo", INS."IdClasificacionTipoInstrumento", 
		INS."IdTipoInstrumento",
		DTA."Tasa", 
		DTA."TasaEstimada", 
		DTA."Plazo", 
		INS."IdMoneda"
UNION 
SELECT
0		AS "IdPrecioTasa",
IFNULL(INS."IdInstrumento",0) AS "IdInstrumento",
DTA."Nemo"											AS "Nemotecnico",
IFNULL(INS."IdClasificacionTipoInstrumento",'IF') 	AS "IdClasificacionTipoInstrumento",
'PAG' 												AS "IdTipoInstrumento",
DTA."Tasa",
0													AS "TasaMod",
DTA."TasaEstimada",
0													AS "TasaEstimadaMod",
DTA."Plazo",
0		AS "PlazoMod",
'CAR'	AS "IdEstado",
IFNULL(INS."IdMoneda",CASE 
		WHEN SUBSTRING(DTA."Nemo",2,1) = 'N' THEN 'CLP' 
		WHEN SUBSTRING(DTA."Nemo",2,1) = '*' THEN 'USD'
		WHEN SUBSTRING(DTA."Nemo",2,1) = 'D' THEN 'USD'
		WHEN SUBSTRING(DTA."Nemo",2,1) = 'E' THEN 'EUR'
		WHEN SUBSTRING(DTA."Nemo",2,1) = 'R' THEN 'EUR'
		WHEN SUBSTRING(DTA."Nemo",2,1) = 'U' THEN 'UF'
	END														)			 AS "IdMoneda"			
FROM "BSCL.Plataforma.BD::GBL.CargarArchivoTasas_Entrada_CargarArchivoTasas" DTA
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
		ON (DTA."Nemo" = INS."Nemotecnico")
	WHERE INS."IdInstrumento" IS NULL
		GROUP BY 
			INS."IdInstrumento", 
			DTA."Nemo", 
			INS."IdClasificacionTipoInstrumento", 
			INS."IdTipoInstrumento",
			DTA."Tasa", 
			DTA."TasaEstimada", 
			DTA."Plazo", 
			INS."IdMoneda";
