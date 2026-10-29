INSERT INTO "BSCL.Normativa.BD::Extornos"
("IdExtornos","FechaOperacion","ModalidadOperacion","NumeroOperacion","CodigoAgente","TipoDetalle","CodigoComision","CantidadCava","CantidadFisi",
"GiroCheq","FlagTrabajador","ComisionContado","ComisionPlazo","FlagPoliza","FlagContado","FlagPlazo","CodigoCustodioValores","CodigoCustodioFondos",
"InteresContado","InteresPlazo","LiqDtc","FlagDayTrade","FechaVencimientoDocumento","IndicadorCartaFianza","MontoCartaFianza","CodigoBanco")
VALUES("BSCL.Normativa.BD.Secuencias::Extornos".NEXTVAL,:FechaOperacion,:ModalidadOperacion,:NumeroOperacion,:CodigoAgente,:TipoDetalle,:CodigoComision,:CantidadCava,:CantidadFisi,
:GiroCheq,:FlagTrabajador,:ComisionContado,:ComisionPlazo,:FlagPoliza,:FlagContado,:FlagPlazo,:CodigoCustodioValores,:CodigoCustodioFondos,
:InteresContado,:InteresPlazo,:LiqDtc,:FlagDayTrade,:FechaVencimientoDocumento,:IndicadorCartaFianza,:MontoCartaFianza,:CodigoBanco);