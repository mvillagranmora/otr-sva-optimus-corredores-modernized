UPDATE "BSCL.Orden.BD::ORD.Instruccion"
SET "IdEstadoInstruccion"     = :IdEstadoPostLiquidacion
  , "FechaUltimaModificacion" = :FechaModificacion
WHERE "IdInstruccion" = :IdInstruccion;
