SELECT "IdCobroAsignacion", "IdAsignacion", "IdCobro", "FechaIngresoCobro", "FechaIngresoRegistro", "FormaCobro", "ValorCobro", "ValorCobroAplicado", "MontoCobroCalculado", "IdCarga", "MontoCobroCalculadoLocal", "MontoCobroCalculadoLiquidacion", "IdMoneda", "ValorCobroFinal", "CodigoCobroClasificacion", "CodigoCobroClasificacionBase"
FROM "BSCL.Orden.BD::ORD.CobroAsignacion"
WHERE "IdAsignacion"=:IdAsignacion;
