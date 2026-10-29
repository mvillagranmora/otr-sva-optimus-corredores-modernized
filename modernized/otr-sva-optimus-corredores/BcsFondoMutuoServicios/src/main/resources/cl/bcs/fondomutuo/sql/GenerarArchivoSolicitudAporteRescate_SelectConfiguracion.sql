
-- Obtiene la ruta donde del servidor donde se debe generar el archivo
SELECT
	MAX(CASE WHEN ("CodigoParametro" = 'PLA.RUTABASEARCHIVOS')  THEN "Valor" ELSE NULL END) AS "RutaBase",
	MAX(CASE WHEN ("CodigoParametro" = 'RUTAARCHIVOSFFMM')      THEN "Valor" ELSE NULL END) AS "RutaFMM",

	(MAX(CASE WHEN ("CodigoParametro" = 'PLA.RUTABASEARCHIVOS') THEN "Valor" ELSE NULL END) ||
	 MAX(CASE WHEN ("CodigoParametro" = 'RUTAARCHIVOSFFMM')      THEN "Valor" ELSE NULL END)) AS "RutaArchivo"
	
FROM
(
	SELECT UCASE("NombreTipoParametro") AS "CodigoParametro", "ValorParametro" AS "Valor"
	FROM "BSCL.FondoMutuo.BD::FMM.Parametro" 
	WHERE UCASE("NombreTipoParametro") = 'RUTAARCHIVOSFFMM'
	AND   "IdEmpresa" = :IdEmpresa   
	
	UNION ALL
	
	SELECT UCASE("CodigoParametro") AS "CodigoParametro", "Valor"
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE UCASE("CodigoParametro") = 'PLA.RUTABASEARCHIVOS' 
	AND  "IdEmpresa" = :IdEmpresa	
);

