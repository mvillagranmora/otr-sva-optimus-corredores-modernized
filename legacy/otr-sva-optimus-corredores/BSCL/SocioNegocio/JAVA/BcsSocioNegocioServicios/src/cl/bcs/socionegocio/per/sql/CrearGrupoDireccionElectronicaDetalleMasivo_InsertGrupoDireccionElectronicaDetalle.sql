INSERT INTO "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle"
    ( "IdGrupoDireccionDetalle"
    , "IdDireccionElectronica"
    , "IdTipoDestinatario"
    , "IdGrupoDireccion"
    , "FechaCreacion"
    , "FechaModificacion"
    , "FechaIngreso"
    , "IdEstado"
    , "IdEstadoAutorizacion"
    , "IdUsuarioCreador")
VALUES
    ( :IdGrupoDireccionDetalle
    , :IdDireccionElectronica
    , :IdTipoDestinatario
    , :IdGrupoDireccion
    , :FechaCreacion
    , :FechaModificacion
    , :FechaIngreso
    , :IdEstado
    , :IdEstadoAutorizacion
    , :IdUsuarioCreador);