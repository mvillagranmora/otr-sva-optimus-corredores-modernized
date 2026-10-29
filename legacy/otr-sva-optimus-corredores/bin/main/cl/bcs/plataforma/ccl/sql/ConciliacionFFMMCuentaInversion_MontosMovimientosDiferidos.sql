SELECT 
SOL."IdSolicitudAporteRescate"
,SOL."Monto" AS "MontoMonedaFondo"
,SOL."CostoMonedaLocal" AS "MontoMonedaSolicitud"
,CA."Monto" AS "MontoCuentaInversion"
,SOL."IdMoneda"
,FONDO."IdMoneda" AS "IdMonedaFondo"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE ON SERIE."IdSerieFondo" = SOL."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FONDO ON FONDO."IdFondo"  = SERIE."IdFondo"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CA ON CA."IdOperacion" = SOL."IdSolicitudAporteRescate"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA on CA."IdCuentaInversion" = CTA."IdCuentaInversion"
WHERE SOL."IdCarga" = 0 AND CA."IdModulo" = 'FMM' AND FONDO."IdMoneda" != SOL."IdMoneda" AND SOL."IdMoneda" = CTA."IdMoneda" AND SOL."Monto" != CA."Monto"
UNION  ALL 
SELECT 
SOL."IdSolicitudAporteRescate"
,SOL."Monto" AS "MontoMonedaFondo"
,SOL."CostoMonedaLocal" AS "MontoMonedaSolicitud"
,CA."Monto" AS "MontoCuentaInversion"
,SOL."IdMoneda"
,FONDO."IdMoneda" AS "IdMonedaFondo"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOL
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIE ON SERIE."IdSerieFondo" = SOL."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FONDO ON FONDO."IdFondo"  = SERIE."IdFondo"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CA ON CA."IdOperacion" = SOL."IdSolicitudAporteRescate"
LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTA on CA."IdCuentaInversion" = CTA."IdCuentaInversion"
WHERE SOL."IdCarga" = 0 AND CA."IdModulo" = 'FMM' AND FONDO."IdMoneda" != SOL."IdMoneda" AND FONDO."IdMoneda" = CTA."IdMoneda" AND SOL."Monto" != CA."Monto"