INSERT INTO "BSCL.Orden.BD::ORD.FormaDePagoOrden"(
"IdOrden","IdPreferenciaPagoPortafolio","IdMedioDePago","IdCuentaBancaria","IdTipoCuentaBancaria","NumeroCuentaBancaria","IdTarjetaBancaria","IdTipoTarjetaBancaria",
"NumeroTarjetaBancaria","IdModalidadDePago","IdBanco")
VALUES(
	:IdOrden,:IdPreferenciaPagoPortafolio,:IdMedioDePago,:IdCuentaBancaria,:IdTipoCuentaBancaria,:NumeroCuentaBancaria,:IdTarjetaBancaria,:IdTipoTarjetaBancaria,
	:NumeroTarjetaBancaria,:IdModalidadDePago,:IdBanco
)
