INSERT INTO "BSCL.Plataforma.BD::DOC.TokenFirmaAnexo"
    ( "IdTokenFirmaAnexo"
    , "IdSolicitudFirmaAnexo"
    , "FechaGeneracion"
    , "FechaExpiracion"
    , "Token")
VALUES
    ( :IdTokenFirmaAnexo
    , :IdSolicitudFirmaAnexo
    , :FechaGeneracion
    , :FechaExpiracion
    , :Token);