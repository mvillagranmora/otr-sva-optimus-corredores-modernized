SELECT 
Tarjeta."IdTarjetaBancaria"
,Tarjeta."IdBanco"
,COALESCE(Banco."NombreBanco",'') AS "Banco"
,COALESCE(Tarjeta."IdTipoTarjetaBancaria",'') AS "IdTipo"
,COALESCE(TipoTarjeta."NombreTipoTarjetaBancaria",'') AS "Tipo"
,Tarjeta."NumeroTarjetaBancaria"
,Tarjeta."CodigoVerificacion"
,CAST(Tarjeta."FechaVencimiento" AS VARCHAR(10)) AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" AS Tarjeta
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS Banco ON Banco."IdBanco" = Tarjeta."IdBanco" AND Banco."IdEmpresa" = :p_IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" AS TipoTarjeta ON TipoTarjeta."IdTipoTarjetaBancaria" = Tarjeta."IdTipoTarjetaBancaria"
WHERE Tarjeta."IdPersona" = :p_IdPersona AND Tarjeta."Borrado"='N'