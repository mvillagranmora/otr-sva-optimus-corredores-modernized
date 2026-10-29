SELECT "IdPortafolio","IdPreferenciaPago","IdCuentaBancaria","IdBancoCuentaBancaria","NombreBancoCuentaBancaria","NumeroCuentaBancaria","IdTipoCuentaBancaria"
"NombreTipoCuentaBancaria","IdTarjetaBancaria","IdTipoTarjetaBancaria","NombreBancoTarjetaBancaria","NumeroTarjetaBancaria" 
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" 
WHERE "IdPortafolio" IN (:IdPortafolio)