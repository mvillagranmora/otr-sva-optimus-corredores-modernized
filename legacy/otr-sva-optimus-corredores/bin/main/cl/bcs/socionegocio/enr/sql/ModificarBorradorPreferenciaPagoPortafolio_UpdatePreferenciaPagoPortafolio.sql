UPDATE "BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio"
SET
"IdPreferenciaPago" = :IdPreferenciaPago
,"IdPersonaRelacionada" = :IdPersonaRelacionada
,"IdCuentaBancaria" = :IdCuentaBancaria
,"IdEmpresa" = :IdEmpresa
,"IdTarjetaBancaria" = :IdTarjetaBancaria
,"PorDefecto" = :PorDefecto
WHERE "IdPreferenciaPagoPortafolio" = :IdPreferenciaPagoPortafolio;