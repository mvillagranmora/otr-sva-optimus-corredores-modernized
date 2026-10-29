SELECT "Valor"
FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio"
WHERE "CodigoParametro" = 'EMP.MaximoCodigoEmpleado' AND "IdEmpresa" = :IdEmpresa
