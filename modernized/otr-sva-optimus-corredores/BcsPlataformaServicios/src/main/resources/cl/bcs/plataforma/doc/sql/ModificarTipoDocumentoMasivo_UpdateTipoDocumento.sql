UPDATE
 "BSCL.Plataforma.BD::DOC.TipoDocumento"
SET "NombreDocumento"        = :NombreDocumento
  , "NumeroFirmasMinimas"    = :NumeroFirmantesMinimo
  , "NumeroFirmasMaximas"    = :NumeroFirmantesMaximo
  , "Glosa"          		 = COALESCE(:Glosa, '')
  , "ValidaFechaVigencia"    = :ValidaFechaVigencia
  , "IdTipoPersona"          = :IdTipoPersona
  , "SolicitarFechaFirma"	 = :SolicitarFechaFirma
  , "IdClase"                = :IdClase
  , "TipoDocumentoNormativa" = :TipoDocumentoNormativa
  , "FechaFirma"             = :FechaFirma
  , "CodigoArchivo"          = :CodigoArchivo
  , "ValidaClienteFull"      = :ValidaClienteFull
  , "RequiereFirmaCliente"   = :RequiereFirmaCliente
  , "BloquearAlVencer"  	 = :BloquearAlVencer
  ,"FirmaAutomaticaDocumento"= :FirmaAutomaticaDocumento
WHERE "IdTipoDocumento" = :IdTipoDocumento
AND "IdEmpresa" = :IdEmpresa;