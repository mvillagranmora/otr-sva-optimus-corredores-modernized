SELECT 
	 RUTBASE."RutaBase"||RUTCTA."RutaCTA" 		AS "RutaArchivo"
	 ,RUTCTA."RutaCTA" 							AS "RutaModulo"
FROM (SELECT 
			1 							AS "IdUnion" 
			,COALESCE("Valor", '') 		AS "RutaBase" 
		FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		WHERE UCASE("CodigoParametro") = 'PLA.RUTABASEARCHIVOS' AND  "IdEmpresa" = :IdEmpresa) AS RUTBASE
INNER JOIN (SELECT 
				1 							AS "IdUnion"
				,COALESCE("Valor", '')		AS  "RutaCTA"
			FROM "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion"
			WHERE UCASE("CodigoParametro") = 'RUTAARCHIVOSCTA' AND "IdEmpresa" = :IdEmpresa) AS RUTCTA
ON RUTBASE."IdUnion" = RUTCTA."IdUnion"