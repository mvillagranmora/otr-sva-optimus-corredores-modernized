UPDATE "BSCL.Plataforma.BD::INS.Corredora" SET "IdPersona" = :IdPersona
WHERE "CodigoInscripcionRegulador" = :CodigoInscripcionRegulador AND "IdPersona" IS NULL;