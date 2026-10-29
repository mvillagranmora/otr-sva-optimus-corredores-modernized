SELECT "IdOrden","IdPreferenciaPagoPortafolio","IdMedioDePago","IdCuentaBancaria","IdTipoCuentaBancaria","NumeroCuentaBancaria","IdTarjetaBancaria","IdTipoTarjetaBancaria","NumeroTarjetaBancaria","IdModalidadDePago","IdBanco"
FROM "BSCL.Orden.BD::ORD.FormaDePagoOrden" where "IdOrden" = :IdOrden

