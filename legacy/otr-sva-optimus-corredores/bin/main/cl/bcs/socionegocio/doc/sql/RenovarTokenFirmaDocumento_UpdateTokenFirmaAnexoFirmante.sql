UPDATE "BSCL.Plataforma.BD::DOC.TokenFirmaAnexo"
SET "Token"           = :Token
  , "FechaGeneracion" = :FechaGeneracion
  , "FechaExpiracion" = :FechaExpiracion
WHERE "IdTokenFirmaAnexo" = :IdTokenFirmaAnexo;