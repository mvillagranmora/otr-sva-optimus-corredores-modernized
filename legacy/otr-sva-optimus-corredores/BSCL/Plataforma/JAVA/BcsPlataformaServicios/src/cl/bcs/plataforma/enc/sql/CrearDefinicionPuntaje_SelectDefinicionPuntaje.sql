SELECT "IdEmpresa", "IdCategoriaInversionista", "PuntajeHasta"
FROM "BSCL.Plataforma.BD::ENC.DefinicionPuntaje"
WHERE "IdCategoriaInversionista" = :IdCategoriaInversionista AND "IdEmpresa" = :IdEmpresa
