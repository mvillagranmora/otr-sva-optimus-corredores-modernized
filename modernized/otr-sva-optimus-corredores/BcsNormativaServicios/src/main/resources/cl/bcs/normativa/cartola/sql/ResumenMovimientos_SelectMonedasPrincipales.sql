SELECT "Valor" AS "IdMoneda" FROM 
"BSCL.Plataforma.BD::PLA.Configuracion"
WHERE "CodigoParametro" IN ('PLA.MonedaSistema','PLA.MonedaAdicional') AND "IdEmpresa" = :IdEmpresa
UNION
SELECT 'EUR' AS "IdMoneda" FROM 
"BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio"
WHERE "CodigoParametro" = 'CLI.CrearCuentaInversionEUR' AND "Valor" = 'S' AND "IdEmpresa" = :IdEmpresa;