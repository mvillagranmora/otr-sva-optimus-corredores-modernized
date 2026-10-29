SELECT
	SOLICITUD."IdSolicitudAporteRescate"
	,SOLICITUD."FechaAceptacion"
	,SOLICITUD."FechaCertificacion"
	,SOLICITUD."FechaLiquidacion"
	,SOLICITUD."IdSerieFondo"
	,SOLICITUD."NumeroCuotas"
	,SOLICITUD."Monto"
	,SOLICITUD."MontoComision"
	,TIPOMOVIMIENTO."Descripcion" AS "DescripcionTipoSolicitud"
	,SERIEDEFONDO."DecimalesCuota"
	,FONDO."IdMoneda" AS "IdMonedaFondo"
	,SOLICITUD."Estado" AS "IdEstado"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SOLICITUD
INNER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo"  TIPOMOVIMIENTO ON (SOLICITUD."IdTipoMovimientoFondoMutuo" = TIPOMOVIMIENTO."IdTipoMovimientoFondoMutuo")
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SERIEDEFONDO ON (SERIEDEFONDO."IdSerieFondo" = SOLICITUD."IdSerieFondo" )
LEFT OUTER  JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON (FONDO."IdFondo" = SERIEDEFONDO."IdFondo" )
WHERE "FechaCertificacion" = :FechaCertificacion