SELECT CuentaBancaria."IdPersona"
,CuentaBancaria."IdCuentaBancaria"
,CuentaBancaria."NumeroCuentaBancaria"
,CuentaBancaria."IdEstado"
,CuentaBancaria."IdMoneda"
,CuentaBancaria."IdTipoCuentaBancaria" AS "CodigoTipoCuentaBancaria"
,TipoCuentaBancaria."NombreTipoCuentaBancaria" AS "TipoCuentaBancaria"
,CuentaBancaria."IdBanco"
,COALESCE(Banco."NombreBanco",'') AS "Banco"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CuentaBancaria
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TipoCuentaBancaria ON TipoCuentaBancaria."IdTipoCuentaBancaria" = CuentaBancaria."IdTipoCuentaBancaria"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" Banco ON Banco."IdBanco" = CuentaBancaria."IdBanco" AND Banco."IdEmpresa" = :p_IdEmpresa
WHERE CuentaBancaria."IdPersona" = :p_IdPersona AND CuentaBancaria."Borrado"='N'