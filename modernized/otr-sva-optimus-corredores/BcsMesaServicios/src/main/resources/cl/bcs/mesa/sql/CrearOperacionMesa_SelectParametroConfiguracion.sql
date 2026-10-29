SELECT 	"CodigoParametro", "Valor" 
FROM 	"BSCL.Plataforma.BD::PLA.Configuracion" 
WHERE 	("CodigoParametro" = 'PLA.MonedaSistema' 
		OR "CodigoParametro" = 'PLA.FechaSistema' 
		OR "CodigoParametro" = 'PLA.LocalizacionSistema') 
		AND "IdEmpresa" = :IdEmpresa
------------------------------------------------------------		
UNION
------------------------------------------------------------
SELECT 	"CodigoParametro", "Valor" 
FROM 	"BSCL.Mesa.BD::MES.ConfiguracionMesa" 
WHERE 	"CodigoParametro" IN ( 'MONTO_MINIMO_PACTO_EN_UF' )
		AND "IdEmpresa" = :IdEmpresa;