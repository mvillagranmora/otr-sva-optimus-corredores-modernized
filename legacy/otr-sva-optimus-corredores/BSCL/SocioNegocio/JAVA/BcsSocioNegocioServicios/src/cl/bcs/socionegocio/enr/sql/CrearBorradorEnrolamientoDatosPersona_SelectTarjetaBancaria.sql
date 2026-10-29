SELECT 
"IdTarjetaBancaria"
,"IdBanco"
,"IdTipoTarjetaBancaria"
,"NumeroTarjetaBancaria"
,"IdPersona"
,"FechaVencimiento"
,"CodigoVerificacion"
FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
WHERE "IdPersona" IN (:IdPersona) AND "Borrado" = 'N';