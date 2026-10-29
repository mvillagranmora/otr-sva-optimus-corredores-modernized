INSERT INTO "BSCL.Orden.BD::ORD.CargaTransaccionROIX"
("IdCargaTransaccionROIX","NombreOrden","NumeroOperacion","Destino","Sucursal","Secuencia","HoraTransaccion","TipoOperacion","Cantidad","Instrumento",
"Precio","Caja","AliasCliente","Operador","LvsQuan","ExQuan","ExPrice","CxlQuan","StopPrice","Firm","ID","MMID","PutCall","OpenClose","ExpMonth",
"StrikePrice","ExecutingBroker","Comision","AwayFlag","BoothID","SubAccount","Terminal","ClOrdID","CustExtra","IdEstado","Resultado")
VALUES("BSCL.Orden.BD.Secuencias::CargaTransaccionROIX".NEXTVAL,:NombreOrden,:NumeroOperacion,:Destino,:Sucursal,:Secuencia,:HoraTransaccion,:TipoOperacion,:Cantidad,
:Instrumento,:Precio,:Caja,:AliasCliente,:Operador,:LvsQuan,:ExQuan,:ExPrice,:CxlQuan,:StopPrice,:Firm,:ID,:MMID,:PutCall,:OpenClose,:ExpMonth,:StrikePrice,:ExecutingBroker,
:Comision,:AwayFlag,:BoothID,:SubAccount,:Terminal,:ClOrdID,:CustExtra,:IdEstado,:Resultado)