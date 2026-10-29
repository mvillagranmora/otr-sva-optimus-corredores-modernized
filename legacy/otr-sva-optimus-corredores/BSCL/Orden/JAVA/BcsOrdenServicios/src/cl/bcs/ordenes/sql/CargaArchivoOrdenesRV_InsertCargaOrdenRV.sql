INSERT INTO "BSCL.Orden.BD::ORD.CargaOrdenRV"
("IdCargaOrdenRV","CodigoLibro","RutCavali","CodigoPortafolio","Vigencia","CondicionLiquidacion","MonedaLiquidacion","TipoInstrumento","Bolsa",
"FormaNegociacion","CodigoProducto","TipoOperacion","Instrumento","Cantidad","Nominal","TipoPrecio","Precio","Comision","FormaPago",
"TipoCustodia","BancoCustodio","BancoLiquida","MedioCaptacion","Representante","IdEstado","Resultado","Boveda","Cuenta")
VALUES("BSCL.Orden.BD.Secuencias::CargaOrdenRV".NEXTVAL,:CodigoLibro,:RutCavali,:CodigoPortafolio,:Vigencia,:CondicionLiquidacion,:MonedaLiquidacion,:TipoInstrumento,:Bolsa,:FormaNegociacion,:CodigoProducto,:TipoOperacion,
:Instrumento,:Cantidad,:Nominal,:TipoPrecio,:Precio,:Comision,:FormaPago,:TipoCustodia,:BancoCustodio,:BancoLiquida,:MedioCaptacion,:Representante,:IdEstado,:Resultado,:Boveda,:Cuenta);