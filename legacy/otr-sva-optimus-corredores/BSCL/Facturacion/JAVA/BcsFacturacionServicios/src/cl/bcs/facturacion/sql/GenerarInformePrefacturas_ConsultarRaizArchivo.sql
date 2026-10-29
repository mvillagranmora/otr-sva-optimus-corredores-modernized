SELECT MAX(CASE WHEN ("CodigoParametro" = 'RUTAARCHIVOSFAC') THEN "Valor" ELSE NULL END)   AS "RutaFAC"
	 , (MAX(CASE WHEN ("CodigoParametro" = 'PLA.RUTABASEARCHIVOS') THEN "Valor" ELSE NULL END) ||
		MAX(CASE WHEN ("CodigoParametro" = 'RUTAARCHIVOSFAC') THEN "Valor" ELSE NULL END)) AS "RutaArchivo"
FROM (
		 SELECT UCASE("CodigoParametro") AS "CodigoParametro", "Valor" AS "Valor"
		 FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
		 WHERE UCASE("CodigoParametro") = UCASE('RUTAARCHIVOSFAC')
		   AND "IdEmpresa" = :IdEmpresa
		   

		 UNION ALL

		 SELECT UCASE("CodigoParametro") AS "CodigoParametro", "Valor"
		 FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		 WHERE UCASE("CodigoParametro") = 'PLA.RUTABASEARCHIVOS'
		   AND "IdEmpresa" = :IdEmpresa
	 );
	 