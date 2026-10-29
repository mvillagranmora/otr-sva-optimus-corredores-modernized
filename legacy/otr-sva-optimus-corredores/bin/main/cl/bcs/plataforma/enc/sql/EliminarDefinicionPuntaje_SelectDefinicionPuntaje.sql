SELECT "IdCategoriaInversionista","IdEmpresa" 
FROM "BSCL.Plataforma.BD::ENC.DefinicionPuntaje"
WHERE "IdEmpresa" = :IdEmpresa AND "IdCategoriaInversionista" = :IdCategoriaInversionista;
