SELECT CAST("DescripcionGrupoActivos" AS NVARCHAR(255)) AS DescripcionGrupoActivos
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivos"
WHERE "IdDetalleGrupoActivos" = :IdDetalleGrupoActivos
