SELECT COUNT(1) AS "countEntidad"
FROM "BSCL.Plataforma.BD::COM.DetalleGrupoActivos"
WHERE "IdInstrumento" = :pIdInstrumento
  AND "IdGrupoActivos" = :pIdGrupoActivos
  AND "IdEstado" = 'ACT'
