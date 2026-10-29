UPDATE "BSCL.Orden.BD::ORD.Instruccion"
SET "IdEstadoInstruccion"     = :IdEstadoPostAnulacion
  , "FechaUltimaModificacion" = :FechaModificacion
  , "IdMotivo" = :IdMotivoPostAnulacion
WHERE "IdInstruccion" = :IdInstruccion;
