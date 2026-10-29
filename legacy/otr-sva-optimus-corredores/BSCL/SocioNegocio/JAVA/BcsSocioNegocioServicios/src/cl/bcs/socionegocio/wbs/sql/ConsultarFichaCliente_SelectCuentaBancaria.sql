SELECT CuentaBancaria."IdPersona"
,COALESCE(CuentaBancaria."IdCuentaBancaria",0) AS "IdCuentaBancaria"
,COALESCE(CuentaBancaria."NumeroCuentaBancaria",'') AS "NumeroCuentaBancaria"
,COALESCE(CuentaBancaria."IdEstado",'') AS "IdEstado"
,COALESCE(CuentaBancaria."IdMoneda",'') AS "IdMoneda"
,COALESCE(CuentaBancaria."IdTipoCuentaBancaria" ,'')AS "CodigoTipoCuentaBancaria"
,COALESCE(TipoCuentaBancaria."NombreTipoCuentaBancaria",'') AS "TipoCuentaBancaria"
,COALESCE(CuentaBancaria."IdBanco",0) AS "IdBanco"
,COALESCE(Banco."NombreBanco",'') AS "Banco"
,COALESCE(Plaza."IdPlaza",0) AS "IdPlaza"
,COALESCE(Plaza."NombrePlaza",'') AS "NombrePlaza"
FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CuentaBancaria
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TipoCuentaBancaria ON TipoCuentaBancaria."IdTipoCuentaBancaria" = CuentaBancaria."IdTipoCuentaBancaria"
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" Banco ON Banco."IdBanco" = CuentaBancaria."IdBanco" AND Banco."IdEmpresa" = :p_IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD::BAN.Plaza" Plaza ON Plaza."IdPlaza" = CuentaBancaria."IdPlaza"
WHERE CuentaBancaria."IdPersona" IN (:p_IdPersonas) AND CuentaBancaria."Borrado"='N'