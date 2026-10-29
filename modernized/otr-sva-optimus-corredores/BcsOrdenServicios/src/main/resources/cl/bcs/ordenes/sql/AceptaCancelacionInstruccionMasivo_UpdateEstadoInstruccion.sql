UPDATE "BSCL.Orden.BD::ORD.Instruccion"
SET "IdEstadoInstruccion"     = :IdEstadoPostAceptacion
  , "FechaUltimaModificacion" = :FechaModificacion
  , "IdMotivo" = :IdMotivoPostAceptacion
WHERE "IdInstruccion" = :IdInstruccion;
