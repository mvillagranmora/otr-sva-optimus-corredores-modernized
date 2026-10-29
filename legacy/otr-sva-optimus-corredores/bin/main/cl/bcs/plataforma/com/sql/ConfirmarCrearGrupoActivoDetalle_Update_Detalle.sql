UPDATE "BSCL.Plataforma.BD::COM.DetalleGrupoActivos"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetalleGrupoActivos" = :IdDetalleGrupoActivos
