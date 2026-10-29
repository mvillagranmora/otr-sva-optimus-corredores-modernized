SELECT 
 COALESCE(CUENTA."IdCuentaBancaria",TO_BIGINT(0)) AS "IdCuentaBancaria"
,COALESCE(CUENTA."IdBanco",TO_BIGINT(0)) AS "IdBanco"
,COALESCE(CUENTA."IdTipoCuentaBancaria",'') AS "IdTipoCuentaBancaria"
,COALESCE(CUENTA."IdMoneda",'') AS "IdMoneda"
,COALESCE(CUENTA."NumeroCuentaBancaria",'') AS "NumeroCuentaBancaria"
,COALESCE(TIPO_CUENTA."NombreTipoCuentaBancaria",'') AS "NombreTipoCuentaBancaria"
FROM  "BSCL.SocioNegocio.BD::PER.CuentaBancaria" AS CUENTA
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TIPO_CUENTA ON TIPO_CUENTA."IdTipoCuentaBancaria" = CUENTA."IdTipoCuentaBancaria"
WHERE CUENTA."IdCuentaBancaria"= :IdCuentaBancaria;