UPDATE "BSCL.SocioNegocio.BD::ENR.CuentaBancaria"
SET
"IdBanco" = :IdBanco
,"IdTipoCuentaBancaria" = :IdTipoCuentaBancaria
,"IdMoneda" = :IdMoneda
,"NumeroCuentaBancaria" = :NumeroCuentaBancaria
,"IdPlaza" = :IdPlaza
,"IdTipoIdentificadorCuenta" = :IdTipoIdentificadorCuenta
,"IdentificadorCuenta" = :IdentificadorCuenta
,"IdPersonaRelacionada" = :IdPersonaRelacionada
,"CodigoCuentaInterbancario" = :CodigoCuentaInterbancario
,"CodigoAba" = :CodigoAba
,"OtroCodigoBancoExtranjero" = :OtroCodigoBancoExtranjero
,"CodigoIban" = :CodigoIban
,"OtroCodigoCuentaExtranjero" = :OtroCodigoCuentaExtranjero
,"EsPac" = :EsPac
,"Predeterminado" = :Predeterminado
WHERE "IdCuentaBancaria" = :IdCuentaBancaria;
