UPDATE "BSCL.Plataforma.BD::IND.Indicador"
SET "ValorIndicador"    = :ValorIndicador
  , "FechaModificacion" = :FechaModificacion
WHERE "IdTipoIndicador" = :IdTipoIndicador
  AND "IdEmpresa" = :IdEmpresa
  AND "IdTipoEntidad" = :IdTipoEntidad
  AND "IdEntidad" = :IdEntidad;