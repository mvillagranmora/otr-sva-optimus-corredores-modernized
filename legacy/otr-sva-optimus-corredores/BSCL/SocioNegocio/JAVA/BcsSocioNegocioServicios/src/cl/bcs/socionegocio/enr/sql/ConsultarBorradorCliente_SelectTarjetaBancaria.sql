SELECT 
"IdTarjetaBancaria"
,"IdBanco"
,"IdTipoTarjetaBancaria"
,"NumeroTarjetaBancaria"
,"IdEnrolamiento"
,TO_VARCHAR("FechaVencimiento",'YYYY-MM-DD') AS "FechaVencimiento"
,TO_BIGINT("CodigoVerificacion") AS "CodigoVerificacion"
,"IdTarjetaBancariaPersona"
FROM "BSCL.SocioNegocio.BD::ENR.TarjetaBancaria"
WHERE "IdEnrolamiento" IN (:IdEnrolamiento);
