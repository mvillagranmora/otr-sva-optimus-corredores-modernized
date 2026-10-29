UPDATE "BSCL.Orden.BD::ORD.Instruccion"
SET "IdEstadoInstruccion"     = :IdEstadoPostRechazo
  , "FechaUltimaModificacion" = :FechaModificacion
  , "IdMotivo" = :IdMotivoPostRechazo
WHERE "IdInstruccion" = :IdInstruccion;
