SELECT
PER."IdPersona"
,IDENT."DocumentoIdentidad"
,COALESCE(CUENTA."IdCuentaBancaria",TO_BIGINT(0)) AS "IdCuentaBancaria"
,COALESCE(CUENTA."IdBanco",TO_BIGINT(0)) AS "IdBanco"
,COALESCE(CUENTA."IdTipoCuentaBancaria",'') AS "IdTipoCuentaBancaria"
,COALESCE(CUENTA."IdMoneda",'') AS "IdMoneda"
,COALESCE(CUENTA."NumeroCuentaBancaria",'') AS "NumeroCuentaBancaria"
,COALESCE(TIPO_CUENTA."NombreTipoCuentaBancaria",'') AS "NombreTipoCuentaBancaria"
FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad" AND IDENT."Predeterminado" ='S'
LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CUENTA ON CUENTA."IdPersona" = PER."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" TIPO_CUENTA ON TIPO_CUENTA."IdTipoCuentaBancaria" = CUENTA."IdTipoCuentaBancaria"
WHERE IDENT."DocumentoIdentidad" = :DocumentoIdentidad;