UPDATE "BSCL.Orden.BD::ORD.CobroAsignacion"
SET 
"IdMoneda"=:IdMoneda, 
"ValorCobro"=:ValorCobro, 
"ValorCobroAplicado"=:ValorCobroAplicado, 
"MontoCobroCalculado"=:MontoCobroCalculado, 
"MontoCobroCalculadoLocal"=:MontoCobroCalculadoLocal, 
"MontoCobroCalculadoLiquidacion"=:MontoCobroCalculadoLiquidacion, 
"ValorCobroFinal"=:ValorCobroFinal 
WHERE "CodigoCobroClasificacionBase" = 'COMISION'
AND "IdAsignacion"=:IdAsignacion
AND "IdCobro"=:IdCobro;