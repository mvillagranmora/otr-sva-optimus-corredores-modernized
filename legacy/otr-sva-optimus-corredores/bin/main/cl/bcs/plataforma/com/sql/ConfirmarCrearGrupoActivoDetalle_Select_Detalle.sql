SELECT "IdDetalleGrupoActivos",
       "IdGrupoActivos",
       "IdInstrumento",
       "IdEstado"
FROM "BSCL.Plataforma.BD::COM.DetalleGrupoActivos"
WHERE "IdDetalleGrupoActivos" = :IdDetalleGrupoActivos
