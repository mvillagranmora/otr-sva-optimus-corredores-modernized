SELECT
"IdTipoCuentaBancaria", 
"NombreTipoCuentaBancaria"
FROM "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoCuentaBancaria"
WHERE "IdTipoCuentaBancaria" in (:IdsTipoCuentaBancaria);