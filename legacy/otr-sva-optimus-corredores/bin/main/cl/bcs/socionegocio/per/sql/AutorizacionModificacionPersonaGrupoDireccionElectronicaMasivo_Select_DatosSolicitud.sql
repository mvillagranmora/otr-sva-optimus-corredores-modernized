SELECT "IdSolicitudCambio" AS "IdSolicitudCambio"
     , "CampoModificado" AS "CampoModificado"
     , "ValorAntiguo" AS "ValorAntiguo"
     , "ValorNuevo" AS "ValorNuevo"
     , "IdEntidadOrigen" AS "IdEntidadOrigen"
     , "IdTipoEntidad" AS "IdTipoEntidad"
     , CAST("IdEntidadOrigen" AS BIGINT) AS "IdGrupoDireccion"
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
WHERE CAST("IdEntidadOrigen" AS BIGINT) IN (:IdGrupoDireccion)
  AND "IdTipoEntidad" = :IdTipoEntidad
  AND "IdEstadoAutorizacion" = :IdEstadoAutorizacion
