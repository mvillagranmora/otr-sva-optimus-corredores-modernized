SELECT MAX(CASE WHEN ("CodigoParametro" = 'SUBRUTAARCHIVOSMODULO') THEN "Valor" ELSE NULL END)   AS "RutaNOR"
	 , (MAX(CASE WHEN ("CodigoParametro" = 'PLA.RUTABASEARCHIVOS') THEN "Valor" ELSE NULL END) ||
		MAX(CASE WHEN ("CodigoParametro" = 'SUBRUTAARCHIVOSMODULO') THEN "Valor" ELSE NULL END)) AS "RutaArchivo"
FROM (
		 SELECT UCASE("CodigoParametro") AS "CodigoParametro", "Valor" AS "Valor"
		 FROM "BSCL.Normativa.BD::NOR.Configuracion"
		 WHERE UCASE("CodigoParametro") = UCASE('SubRutaArchivosModulo')
		   AND "IdEmpresa" = :IdEmpresa

		 UNION ALL

		 SELECT UCASE("CodigoParametro") AS "CodigoParametro", "Valor"
		 FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		 WHERE UCASE("CodigoParametro") = 'PLA.RUTABASEARCHIVOS'
		   AND "IdEmpresa" = :IdEmpresa
	 );
	