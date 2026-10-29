SELECT CONFIG."Valor"||CONFIGCTAINV."Valor" AS "RutaArchivo", CONFIGCTAINV."Valor" AS "RutaModulo" 
FROM "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIG
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion" AS CONFIGCTAINV ON (CONFIGCTAINV."IdEmpresa" = CONFIG."IdEmpresa") 
	WHERE UCASE(CONFIG."CodigoParametro") = 'PLA.RUTABASEARCHIVOS' AND UCASE(CONFIGCTAINV."CodigoParametro") = 'RUTAARCHIVOSCTA' 
	AND CONFIG."IdEmpresa" = :IdEmpresa;