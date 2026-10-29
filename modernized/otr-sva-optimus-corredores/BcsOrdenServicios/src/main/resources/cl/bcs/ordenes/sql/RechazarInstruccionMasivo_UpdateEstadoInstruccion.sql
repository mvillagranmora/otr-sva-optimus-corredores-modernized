UPDATE "BSCL.Orden.BD::ORD.Instruccion"
SET "IdEstadoInstruccion"     = :IdEstadoPostRechazo
  , "FechaUltimaModificacion" = :FechaModificacion
WHERE "IdInstruccion" = :IdInstruccion;
