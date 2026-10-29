INSERT INTO "BSCL.Orden.BD::ORD.CargaAsignacionRV"
("IdCargaTransaccionesRV","CodigoLibro","FechaSubasta","FechaEmision","FechaPago","RutCavali","CodigoPortafolio","Mercado","Producto","Bolsa","TipoOperacion","Instrumento",
"Cantidad","Precio","Moneda","Nominal","FolioTransaccion","BLV","Cavali","Smv","FondoLiquidacion","FondoGarantias","ComisionSab","IdEstado","Resultado","Boveda","Cuenta")
VALUES("BSCL.Orden.BD.Secuencias::CargaAsignacionRV".NEXTVAL,:CodigoLibro,:FechaSubasta,:FechaEmision,:FechaPago,:RutCavali,:CodigoPortafolio,:Mercado,:Producto,:Bolsa,:TipoOperacion,:Instrumento,
:Cantidad,:Precio,:Moneda,:Nominal,:FolioTransaccion,:BLV,:Cavali,:Smv,:FondoLiquidacion,:FondoGarantias,:ComisionSab,:IdEstado,:Resultado,:Boveda,:Cuenta);