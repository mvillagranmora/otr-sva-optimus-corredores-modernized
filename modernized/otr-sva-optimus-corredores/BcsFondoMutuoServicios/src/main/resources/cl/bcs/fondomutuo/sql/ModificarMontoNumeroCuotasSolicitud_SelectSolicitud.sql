SELECT 
SOL."IdSolicitudAporteRescate"
,SOL."CalculoPorMontoCuota"
,SOL."IdMoneda"
,FONDO."IdMoneda" AS "IdMonedaFondo"
,SOL."IdMonedaPago"
,SOL."FechaCertificacion"
,SOL."FechaValorCuota"
,SOL."IdSerieFondo"
,SOL."TipoSolicitud"
,SOL."IdTipoMovimientoFondoMutuo"
,SOL."IdSolicitudAporteRescateRelacionado"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SOL
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIE ON SERIE."IdSerieFondo" = SOL."IdSerieFondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON (FONDO."IdFondo" = SERIE."IdFondo" )
WHERE SOL."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)