SELECT CAST("DescripcionGrupoActivos" AS NVARCHAR(255)) AS DescripcionGrupoActivos
FROM "BSCL.Plataforma.BD::COM.GrupoActivos"
WHERE "IdGrupoActivos" = :IdGrupoActivos
