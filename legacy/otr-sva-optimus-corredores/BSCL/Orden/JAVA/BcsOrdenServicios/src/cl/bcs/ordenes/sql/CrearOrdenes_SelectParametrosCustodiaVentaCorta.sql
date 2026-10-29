SELECT TOP 1 "IdTipoPrestamo","MinimoDiasPrestamo" AS "VentaCortaPlazoMinimo","MaximoDiasPrestamo" AS "VentaCortaPlazoMaximo"
FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarTipoPrestamo"
WHERE "IdTipoPrestamo" = :IdTipoPrestamoVentaCorta AND "IdEstado" = 'ACT';