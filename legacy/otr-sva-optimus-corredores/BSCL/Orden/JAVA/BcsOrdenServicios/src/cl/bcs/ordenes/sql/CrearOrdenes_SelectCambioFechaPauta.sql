SELECT TOP 1 COALESCE("FechaFin",TO_TIMESTAMP('0001-01-01 04:00:01','YYYY-MM-DD HH24:MI:SS'))  AS "UltimoProcesoCambioFecha"
FROM "BSCL.Plataforma.BD::PAU.EjecucionDetalle" 
WHERE "CodigoProceso" = 'CAMBIO_FECHA_SISTEMA'
ORDER BY "IdEjecucion" DESC