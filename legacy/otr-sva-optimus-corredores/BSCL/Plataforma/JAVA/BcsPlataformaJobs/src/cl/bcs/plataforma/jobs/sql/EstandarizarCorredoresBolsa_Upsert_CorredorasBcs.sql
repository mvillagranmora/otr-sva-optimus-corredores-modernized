UPSERT "BSCL.Plataforma.BD::INS.Corredora"("IdCorredora", "IdPersona", "CodigoInscripcionRegulador", "IdEstado", "Borrado", "NombreCorto")
VALUES(:IdCorredora, :IdPersona, :CodigoInscripcionRegulador, 'ACT', 'N', :NEMO)
WHERE "IdCorredora" = :IdCorredora AND "CodigoInscripcionRegulador" = :CodigoInscripcionRegulador AND "IdPersona" IS NULL;