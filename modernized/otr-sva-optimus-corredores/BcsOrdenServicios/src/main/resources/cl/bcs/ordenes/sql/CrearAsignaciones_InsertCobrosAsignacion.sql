/********************************************************** 
Proposito	: consulta de cobros para asignaciones
autor		: RU
fecha		: 01/02/2017
***********************************************************/
INSERT INTO "BSCL.Orden.BD::ORD.CobroAsignacion" (
"IdCobroAsignacion","IdAsignacion","IdCobro","FechaIngresoCobro","FechaIngresoRegistro","FormaCobro","IdMoneda","CodigoCobroClasificacion",
"ValorCobro","ValorCobroAplicado","ValorCobroFinal","MontoCobroCalculado","MontoCobroCalculadoLocal","MontoCobroCalculadoLiquidacion",
"CodigoCobroClasificacionBase")
VALUES(:IdCobroAsignacion,:IdAsignacion,:IdCobro,:FechaIngresoCobro,CURRENT_UTCTIMESTAMP,:FormaCobro,:IdMoneda,:CodigoCobroClasificacion,
:ValorCobro,:ValorCobroAplicado,:ValorCobroFinal,:MontoCobroCalculado,:MontoCobroCalculadoLocal,:MontoCobroCalculadoLiquidacion,
:CodigoCobroClasificacionBase);
			
