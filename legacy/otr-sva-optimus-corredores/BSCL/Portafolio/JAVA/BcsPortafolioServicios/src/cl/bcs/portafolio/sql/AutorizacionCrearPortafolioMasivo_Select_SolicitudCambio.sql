-- Consultar datos de SolicitudCambio para portafolios MODPOR
SELECT "IdEntidadOrigen" AS "IdEntidadOrigen",
       "IdTipoEntidad" AS "IdTipoEntidad"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"
WHERE CAST("IdEntidadOrigen" AS NVARCHAR(40)) IN (:IdsPortafolio)
  AND "IdTipoEntidad" = 'PORTAFOLIO'
  AND "IdEstadoAutorizacion" = 'PEN'
