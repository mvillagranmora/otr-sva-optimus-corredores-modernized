INSERT INTO "BSCL.SocioNegocio.BD::PER.GrupoDireccion"
    ( "IdGrupoDireccion"
    , "Grupo"
    , "Comentario"
    , "IdPersona"
    , "FechaCreacion"
    , "FechaModificacion"
    , "FechaIngreso"
    , "IdEstado"
    , "IdEstadoAutorizacion"
    , "IdUsuarioCreador")
VALUES
    ( :IdGrupoDireccion
    , :Grupo
    , :Comentario
    , :IdPersona
    , :FechaCreacion
    , :FechaModificacion
    , :FechaIngreso
    , :IdEstado
    , :IdEstadoAutorizacion
    , :IdUsuarioCreador);