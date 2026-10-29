SELECT EMI."IdEmisor", PER."IdPaisResidencia" AS "IdPaisEmisor", TO_BIGINT(CON."Valor") AS "IdPaisSistema"
FROM "BSCL.Plataforma.BD::MER.Emisor" EMI
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = EMI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CON ON "CodigoParametro"='PLA.PaisSistema'
WHERE EMI."IdEmisor" = :IdEmisor;