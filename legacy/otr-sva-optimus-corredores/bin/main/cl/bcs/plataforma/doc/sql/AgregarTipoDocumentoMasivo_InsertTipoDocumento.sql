INSERT INTO "BSCL.Plataforma.BD::DOC.TipoDocumento"
    ( "IdTipoDocumento"
    , "IdEmpresa"
    , "NombreDocumento"
    , "NumeroFirmasMinimas"
    , "NumeroFirmasMaximas"
    , "Glosa"
    , "ValidaFechaVigencia"
    , "IdTipoPersona"
    , "SolicitarFechaFirma"
    , "IdClase"
    , "TipoDocumentoNormativa"
    , "FechaFirma"
    , "CodigoArchivo"
    , "ValidaClienteFull"
    , "RequiereFirmaCliente"
    , "BloquearAlVencer"
    , "FirmaAutomaticaDocumento")
VALUES
    ( :IdTipoDocumento
    , :IdEmpresa
    , :NombreDocumento
    , :NumeroFirmantesMinimo
    , :NumeroFirmantesMaximo
    , COALESCE(:Glosa, '')
    , :ValidaFechaVigencia
    , :IdTipoPersona
    , :SolicitarFechaFirma
    , :IdClase
    , :TipoDocumentoNormativa
    , :FechaFirma
    , :CodigoArchivo
    , :ValidaClienteFull
    , :RequiereFirmaCliente
    , :BloquearAlVencer
    , :FirmaAutomaticaDocumento)