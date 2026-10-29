UPDATE "BSCL.Orden.BD::ORD.CobroOrden"
SET 
"MontoMinimo"=:MontoMinimo, 
"ValorCobroPerfil"=:ValorCobroPerfil, --%
"ValorCobroAplicado"=:ValorCobroAplicado, --%
"ValorCobroFinal"=:ValorCobroFinal, --%
"MontoCobroCalculado"=:MontoCobroCalculado, 
"MontoCobroCalculadoLocal"=:MontoCobroCalculadoLocal, 
"MontoCobroCalculadoLiquidacion"=:MontoCobroCalculadoLiquidacion
WHERE "CodigoCobroClasificacionBase" = 'COMISION'
AND "IdOrden"=:IdOrden
AND "IdCobro"=:IdCobro;