SELECT
"IdTipoTarjetaBancaria", 
"NombreTipoTarjetaBancaria"
FROM "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoTarjetaBancaria"
WHERE "IdTipoTarjetaBancaria" in (:IdsTipoTarjetaBancaria);