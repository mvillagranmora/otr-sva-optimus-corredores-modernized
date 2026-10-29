INSERT INTO "BSCL.Orden.BD::ORD.CobroOrden"(
"IdCobroOrden","IdGrupoCobro","IdOrden","IdCobro","ValorCobroPerfil","ValorCobroAplicado",
"MontoCobroCalculado","MontoCobroCalculadoLocal","MontoCobroCalculadoLiquidacion","FormaCobro", 
"EsComprobante","FechaIngresoCobro", "MontoMinimo", "CodigoCobroClasificacion", "CodigoCobroClasificacionBase","IdMoneda","IdOrigenCobro","ValorCobroFinal","IdAsociacion","PermiteCambiarValor")
VALUES(
"BSCL.Orden.BD.Secuencias::CobrosOrden".NEXTVAL,:IdGrupoCobro,:IdOrden,:IdCobro,:ValorCobroPerfil,:ValorCobroAplicado,
:MontoCobroCalculado,:MontoCobroCalculadoLocal,:MontoCobroCalculadoLiquidacion,:FormaCobro,
:EsComprobante,CURRENT_UTCTIMESTAMP,:MontoMinimo,:CodigoCobroClasificacion,:CodigoCobroClasificacionBase,:IdMoneda,:IdOrigenCobro,:ValorCobroFinal,COALESCE(:IdAsociacion,0),:PermiteCambiarValor
);
