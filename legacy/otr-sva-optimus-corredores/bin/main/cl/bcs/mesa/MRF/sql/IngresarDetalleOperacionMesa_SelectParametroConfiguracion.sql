SELECT "CodigoParametro", "Valor" 
FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
WHERE ("CodigoParametro" = 'PLA.MonedaSistema' 
	OR "CodigoParametro" = 'PLA.FechaSistema' 
	OR "CodigoParametro" = 'PLA.LocalizacionSistema') 
AND "IdEmpresa" = :IdEmpresa;