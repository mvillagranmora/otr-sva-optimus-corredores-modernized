SELECT "FechaDesde", "FechaHasta" 
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarPeriodosComisiones" 
WHERE "IdPeriodo" = :IdPeriodo 