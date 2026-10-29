INSERT INTO "BSCL.Normativa.BD::OperacionesNegociadas"
("IdOperacionesNegociadas","FechaOperacion","Modalidad","NumeroOperacion","TipoMoneda","TipoOperacion","TipoInstrumento","EstadoOperacion",
"CodigoValor","Nemotecnico","Precio","Cantidad","AgenteComprador","AgenteVendedor","MontoEfectivo","ValorNominal",
"TasaOperacion","FechaLiquidacionContado","FechaLiquidacionPlazo","CantidadDiasOperacion","TipoAval","CodigoAval"					
,"TipoLiquidacion","IndicadorHabilitacionFondos","TipoCambio","DescripcionValorNegociado","HoraOperacion","InteresesCorridosContadoPlazo"	
,"NumeroSubasta","AgenteCompradorEspecialista","AgenteVendedorEspecialista","FlagOfertaPublica")
VALUES(
"BSCL.Normativa.BD.Secuencias::OperacionesNegociadas".NEXTVAL,:FechaOperacion,:Modalidad,:NumeroOperacion,:TipoMoneda,:TipoOperacion,:TipoInstrumento,:EstadoOperacion,
:CodigoValor,:Nemotecnico,:Precio,:Cantidad,:AgenteComprador,:AgenteVendedor,:MontoEfectivo,:ValorNominal,
:TasaOperacion,:FechaLiquidacionContado,:FechaLiquidacionPlazo,:CantidadDiasOperacion,:TipoAval,:CodigoAval,
:TipoLiquidacion,:IndicadorHabilitacionFondos,:TipoCambio,:DescripcionValorNegociado,:HoraOperacion,:InteresesCorridosContadoPlazo,
:NumeroSubasta,:AgenteCompradorEspecialista,:AgenteVendedorEspecialista,:FlagOfertaPublica
);