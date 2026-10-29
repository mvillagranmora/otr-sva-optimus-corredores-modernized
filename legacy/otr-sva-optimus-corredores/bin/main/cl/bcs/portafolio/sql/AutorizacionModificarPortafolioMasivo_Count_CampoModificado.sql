-- Count Campo Modificado
SELECT COUNT("ValorNuevo") AS "Contador"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
WHERE CAST(SOL."IdEntidadOrigen" AS BIGINT) IN (:IdPortafolios)
  AND SOL."CampoModificado" = :CampoModificado
  AND SOL."IdTipoEntidad" = :IdTipoEntidad
  AND SOL."IdEstadoAutorizacion" = :IdEstadoAutorizacion
