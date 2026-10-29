SELECT "FolioPrestamo", "CantidadAsignada"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
WHERE "FolioPrestamo" IN (:FolioPrestamo);