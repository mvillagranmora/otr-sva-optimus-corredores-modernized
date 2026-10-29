-- Obtiene configuracion para la ruta donde se debe generar el archivo
SELECT
	MAX(CASE WHEN ("CodigoParametro" = 'PLA.RUTABASEARCHIVOS') THEN "Valor" ELSE NULL END) AS "RutaBase",
	MAX(CASE WHEN ("CodigoParametro" = 'RUTAARCHIVOSCTA')      THEN "Valor" ELSE NULL END) AS "RutaCta",

	(MAX(CASE WHEN ("CodigoParametro" = 'PLA.RUTABASEARCHIVOS') THEN "Valor" ELSE NULL END) ||
	 MAX(CASE WHEN ("CodigoParametro" = 'RUTAARCHIVOSCTA')      THEN "Valor" ELSE NULL END)) AS "RutaArchivo"
	
FROM
(
	SELECT UCASE("CodigoParametro") AS "CodigoParametro", "Valor"
	FROM "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion"
	WHERE UCASE("CodigoParametro") = 'RUTAARCHIVOSCTA' 
	AND   "IdEmpresa" = :IdEmpresa
	
	UNION ALL
	
	SELECT UCASE("CodigoParametro") AS "CodigoParametro", "Valor"
	FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	WHERE UCASE("CodigoParametro") = 'PLA.RUTABASEARCHIVOS' 
	AND  "IdEmpresa" = :IdEmpresa
);