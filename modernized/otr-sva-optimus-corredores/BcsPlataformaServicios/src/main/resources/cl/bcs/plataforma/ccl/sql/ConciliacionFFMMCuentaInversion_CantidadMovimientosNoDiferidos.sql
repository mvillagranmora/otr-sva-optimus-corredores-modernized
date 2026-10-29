SELECT 
SOL."IdSolicitudAporteRescate"
,COUNT(CA."IdCargoAbonoMovimiento") AS "CantidadCargoAbono"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE ON SERIE."IdSerieFondo" = SOL."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FONDO ON FONDO."IdFondo"  = SERIE."IdFondo"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CA ON CA."IdOperacion" = SOL."IdSolicitudAporteRescate"
WHERE SOL."IdCarga" = 0 AND CA."IdModulo" = 'FMM' AND FONDO."IdMoneda" = SOL."IdMoneda"
GROUP BY SOL."IdSolicitudAporteRescate"
HAVING COUNT(1) > 1
UNION ALL 
SELECT 
SOL."IdSolicitudAporteRescate"
,TO_BIGINT(0) AS "CantidadCargoAbono"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE ON SERIE."IdSerieFondo" = SOL."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FONDO ON FONDO."IdFondo"  = SERIE."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" TPO ON TPO."IdTipoMovimientoFondoMutuo" = SOL."IdTipoMovimientoFondoMutuo"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CA ON CA."IdOperacion" = SOL."IdSolicitudAporteRescate" AND CA."IdModulo" = 'FMM'
WHERE SOL."IdCarga" = 0 AND CA."IdOperacion" IS NULL AND SOL."Estado" = 'CRT' AND TPO."TieneCertificacion" = 'S' AND FONDO."IdMoneda" = SOL."IdMoneda";