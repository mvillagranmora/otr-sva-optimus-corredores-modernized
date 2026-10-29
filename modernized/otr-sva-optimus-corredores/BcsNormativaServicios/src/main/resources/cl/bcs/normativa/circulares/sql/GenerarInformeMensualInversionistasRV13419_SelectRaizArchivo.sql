SELECT "Valor" AS "RutaArchivo"
		 FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		 WHERE UCASE("CodigoParametro") = 'PLA.RUTABASEARCHIVOS'
		 AND "IdEmpresa" = :IdEmpresa;