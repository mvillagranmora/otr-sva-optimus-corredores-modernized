INSERT INTO "BSCL.Orden.BD::ORD.CargaTransaccionTOMS"
("IdCargaTransaccionTOMS","NumeroOperacion","CodigoPortafolio","FechaOperacion","FechaOperacionHora","ISIN","TipoOperacion","FechaLiquidacion","IdMoneda","Cantidad","TraderPrice",
"Principal","Interes","LongNote1","MontoComision","Yield","TipoTransaccion","CommissionRate","BrokerId","TraderLogin","ReasonCode","Mercado","Filler","MasterTicket","TransCpartyShortName",
"NumeroOperacionOrigen","Action","TipoCambio","TraderPprice","SalesName","ExchangeRate","ShortCover","IdEstado","Resultado")
VALUES("BSCL.Orden.BD.Secuencias::CargaTransaccionTOMS".NEXTVAL,:NumeroOperacion,:CodigoPortafolio,:FechaOperacion,:FechaOperacionHora,:ISIN,:TipoOperacion,:FechaLiquidacion,:IdMoneda,:Cantidad,:TraderPrice,
:Principal,:Interes,:LongNote1,:MontoComision,:Yield,:TipoTransaccion,:CommissionRate,:BrokerId,:TraderLogin,:ReasonCode,:Mercado,:Filler,:MasterTicket,:TransCpartyShortName,
:NumeroOperacionOrigen,:Action,:TipoCambio,:TraderPprice,:SalesName,:ExchangeRate,:ShortCover,:IdEstado,:Resultado)