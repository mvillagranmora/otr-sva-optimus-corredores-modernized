SELECT "IdIngresoEgreso", CAST("FechaMovimiento" AS DATE) AS "FechaMovimiento", "IdEstado", "FechaHoraAnulacion", "IdUsuarioAnulacion"
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso"
WHERE "IdEmpresa" = :p_IdEmpresa
  AND "FechaMovimiento" <= :p_Fecha
  AND "IdEstado" = 'ING';