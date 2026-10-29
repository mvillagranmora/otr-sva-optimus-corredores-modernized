UPDATE "BSCL.Plataforma.BD::COM.DetalleGrupoActivos"
SET "IdInstrumento" = COALESCE(:IdInstrumento, "IdInstrumento"),
    "IdEstado" = COALESCE(:IdEstado, "IdEstado"),
    "FechaModificacion" = CURRENT_UTCTIMESTAMP,
    "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdDetalleGrupoActivos" = :IdDetalleGrupoActivos
