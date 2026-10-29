INSERT
INTO "BSCL.SocioNegocio.BD::ENR.GrupoDireccion"
    ( "IdGrupoDireccion"
    , "Grupo"
    , "Comentario"
    , "IdEnrolamiento"
    , "IdGrupoDireccionPersona"
    )
VALUES
    ( :IdGrupoDireccion
    , :Grupo
    , :Comentario
    , :IdEnrolamiento
    , :IdGrupoDireccionPersona
    );
