SELECT "IdSolicitudCambio", "IdEntidadOrigen", "IdTipoEntidad", "IdEstadoAutorizacion", "CampoModificado", "ValorAntiguo", "ValorNuevo"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"
WHERE "IdEntidadOrigen" IN (:p_IdEntidadOrigen) AND "IdTipoEntidad" IN ('TARJETABANCARIA','TARJETABANCARIA-ESTADO') AND "IdEstadoAutorizacion" = 'PEN'
