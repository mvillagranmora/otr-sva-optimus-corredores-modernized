SELECT COUNT(1) AS "Contador"
FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio"
WHERE CAST("IdEntidadOrigen" AS BIGINT) IN (:IdGrupoDireccion)
  AND "IdTipoEntidad" = :IdTipoEntidad
  AND "IdEstadoAutorizacion" = :IdEstadoAutorizacion
