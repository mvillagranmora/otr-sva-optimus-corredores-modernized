SELECT "IdEmpresa","CodigoParametro","Descripcion","Valor"
FROM "BSCL.Custodia.BD::CUS.Configuracion"
WHERE "IdEmpresa" = :p_IdEmpresa 
  AND "CodigoParametro" IN (:p_CodigoParametro);  