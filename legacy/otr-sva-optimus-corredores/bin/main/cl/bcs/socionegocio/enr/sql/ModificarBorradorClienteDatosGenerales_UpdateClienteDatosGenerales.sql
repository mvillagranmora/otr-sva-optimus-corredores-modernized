UPDATE "BSCL.SocioNegocio.BD::ENR.Enrolamiento"
SET "CLI_IdTipoCliente"            = :IdTipoCliente
  , "CLI_IdCategoriaInversionista" = :IdCategoriaInversionista
  , "CLI_IdSegmentoCliente"        = :IdSegmentoCliente
  , "CLI_IdEmpleadoEjecutivo"      = :IdEmpleadoEjecutivo
  , "CLI_CodigoEjecutivoExterno"   = :CodigoEjecutivoExterno
  , "CLI_IdSucursalPreferencia"    = :IdSucursalPreferencia
  , "CLI_EsClienteFinal"           = :EsClienteFinal
  , "CLI_AreaCliente"			   = :CodigoArea
WHERE "IdEnrolamiento" = :IdEnrolamiento;