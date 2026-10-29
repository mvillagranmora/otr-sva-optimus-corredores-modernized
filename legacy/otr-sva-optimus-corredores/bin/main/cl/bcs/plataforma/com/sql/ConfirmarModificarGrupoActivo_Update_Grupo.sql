UPDATE "BSCL.Plataforma.BD::COM.GrupoActivos"
SET "DescripcionGrupoActivos" = COALESCE(:DescripcionGrupoActivos, "DescripcionGrupoActivos"),
    "IdTipoGrupoActivos" = COALESCE(:IdTipoGrupoActivos, "IdTipoGrupoActivos"),
    "IdEstado" = COALESCE(:IdEstado, "IdEstado"),
    "PorDefecto" = COALESCE(:PorDefecto, "PorDefecto"),
    "FechaModificacion" = CURRENT_UTCTIMESTAMP,
    "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdGrupoActivos" = :IdGrupoActivos
  AND "IdEmpresa" = :IdEmpresa
