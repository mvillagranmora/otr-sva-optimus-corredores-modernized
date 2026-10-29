SELECT 
Tarjeta."IdPersona"
,COALESCE(Tarjeta."IdTarjetaBancaria",0) AS "IdTarjetaBancaria"
,COALESCE(Tarjeta."IdBanco",0) AS "IdBanco"
,COALESCE(Banco."NombreBanco",'') AS "Banco"
,COALESCE(Tarjeta."IdTipoTarjetaBancaria",'') AS "IdTipo"
,COALESCE(TipoTarjeta."NombreTipoTarjetaBancaria",'') AS "Tipo"
,COALESCE(Tarjeta."NumeroTarjetaBancaria",'') AS "NumeroTarjeta"
,COALESCE(Tarjeta."CodigoVerificacion",0) AS "CodigoVerificacion"
,CAST(Tarjeta."FechaVencimiento" AS VARCHAR(10)) AS "FechaVencimiento"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" AS Tarjeta
LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" AS Banco ON Banco."IdBanco" = Tarjeta."IdBanco" AND Banco."IdEmpresa" = :p_IdEmpresa
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" AS TipoTarjeta ON TipoTarjeta."IdTipoTarjetaBancaria" = Tarjeta."IdTipoTarjetaBancaria"
WHERE Tarjeta."IdPersona" IN(:p_IdPersonas) AND Tarjeta."Borrado"='N'