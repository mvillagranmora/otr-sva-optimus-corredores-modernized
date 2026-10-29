SELECT "Valor" AS "Valor" FROM "BSCL.Orden.BD::ORD.ConfiguracionOrdenes" 
WHERE 
"CodigoParametro" = 'ToleranciaErroresCarga'
AND "IdEmpresa" = :IdEmpresa