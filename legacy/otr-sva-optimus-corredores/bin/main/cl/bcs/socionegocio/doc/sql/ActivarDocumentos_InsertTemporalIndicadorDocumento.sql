INSERT INTO "BSCL.Plataforma.BD::DOC.ConfirmarIndicadorDocumentoCliente"
    ( "IdTrabajo"
    , "IdEmpresa"
    , "IdTipoDocumento"
    , "IdTipoIndicador"
    , "IdCliente")
VALUES
    ( :IdTrabajo
    , :IdEmpresa
    , :IdTipoDocumento
    , :IdTipoIndicador
    , :IdCliente);