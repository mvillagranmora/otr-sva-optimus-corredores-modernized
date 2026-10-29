SELECT "Valor"
      ,"CodigoParametro"
FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa"
WHERE "CodigoParametro" IN (:CodigoParametros)
AND "IdEmpresa" = :IdEmpresa;