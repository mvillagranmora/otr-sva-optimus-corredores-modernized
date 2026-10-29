UPDATE "BSCL.Plataforma.BD::ENC.DefinicionPuntaje"
SET "PuntajeHasta" = :PuntajeHasta
WHERE "IdEmpresa" = :IdEmpresa AND "IdCategoriaInversionista" = :IdCategoriaInversionista;
