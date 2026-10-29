UPDATE "BSCL.Plataforma.BD::PLA.SolicitudCambio"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdEntidadOrigen" = :IdEntidadOrigen AND "IdTipoEntidad" = :IdTipoEntidad
AND "IdEstadoAutorizacion" IN ('PEN', 'ING');