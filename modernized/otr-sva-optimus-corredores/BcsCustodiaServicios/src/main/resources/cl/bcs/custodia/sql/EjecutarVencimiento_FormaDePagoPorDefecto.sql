SELECT TOP 1 "IdMedioDePago",
       "IdCuentaBancaria",
       "IdTipoCuentaBancaria",
       "NombreTipoCuentaBancaria",
       "IdBancoCuentaBancaria",
       "NumeroCuentaBancaria"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio"
WHERE "IdPortafolio" = :IdPortafolio
  AND "IdEmpresa" = :IdEmpresa
  AND "IdEstado" = 'ACT'
  AND "PorDefecto" = 'S';