SELECT "CampoModificado",
       "ValorAntiguo",
       "ValorNuevo"
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
WHERE "IdTipoEntidad" = :IdTipoEntidad
  AND "IdEntidadOrigen" = :IdEntidadOrigen
ORDER BY "IdSecuenciaCambio"
