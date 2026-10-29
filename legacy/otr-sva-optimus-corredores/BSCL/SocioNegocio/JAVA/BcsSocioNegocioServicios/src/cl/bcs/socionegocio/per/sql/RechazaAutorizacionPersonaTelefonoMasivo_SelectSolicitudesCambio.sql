SELECT SOL."IdSolicitudCambio" AS "IdSolicitudCambio",
       SOL."IdEntidadOrigen" AS "IdEntidadOrigen",
       SOL."IdTipoEntidad" AS "IdTipoEntidad",
       SOL."IdEstadoAutorizacion" AS "IdEstadoAutorizacion",
       TEL."IdPersona" AS "IdPersona"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
INNER JOIN "BSCL.SocioNegocio.BD::PER.Telefono" TEL 
    ON CAST(TEL."IdTelefono" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
WHERE SOL."IdEntidadOrigen" = :p_IdEntidadOrigen
  AND SOL."IdTipoEntidad" = :p_IdTipoEntidad
  AND SOL."IdEstadoAutorizacion" = 'PEN';
