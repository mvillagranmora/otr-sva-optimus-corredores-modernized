SELECT "IdEmpresa"      
     , "IdTipoProducto"     
     , "CodigoParametro" 
     , "Descripcion"    
     , "Valor"
     , "ModificadorParametro"
FROM "BSCL.Mesa.BD::MES.ConfiguracionMesa"
WHERE "IdTipoProducto" IN (:IdTipoProducto) AND "CodigoParametro" = (:CodigoParametro) AND "IdEmpresa" IN (:IdEmpresa);