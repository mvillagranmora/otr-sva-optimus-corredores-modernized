UPDATE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
SET
    "FechaCertificacionRegistro" = CURRENT_UTCTIMESTAMP,
	"Monto" = :Monto,
	"ValorCuota" = :ValorCuota,
	"NumeroCuotas" = :NumeroCuotas,
	"ParidadFechaCertificacion" = COALESCE(:ParidadFechaCertificacion,0),
	"CostoMonedaLocal" = :CostoMonedaLocal,
	"MontoPago" = :MontoPago,
	"MontoPagoLocal" = :MontoPagoLocal,
	"IdSolicitudAporteRescateRelacionado" = :IdSolicitudAporteRescateRelacionado,
	"Spread" = CASE "TipoSolicitud" WHEN 'RES' THEN :Spread ELSE 0 END,
	"CostoUF" = CASE WHEN "TipoSolicitud" IN ('RES','APO') THEN :CostoUF ELSE "CostoUF" END,
	"RentabilidadReal" = :RentabilidadReal,
	"IdMonedaSistema" = :IdMonedaSistema,
	"MontoMonedaSistema" = :MontoMonedaSistema,
	"IdIngresoEgreso"=:IdIngresoEgreso,
	"IdOperacionSpot"=:IdOperacionSpot,
	"IdCartera"=:IdCartera,
	"MontoComision"=:MontoComision,
	"MontoComisionLocal"=:MontoComisionLocal,
	"ValorUFFechaInversion" = CASE "IdTipoMovimientoFondoMutuo" WHEN 'APOREINV' THEN :ValorUFFechaInversion ELSE "ValorUFFechaInversion" END,
	"Estado" = 'CRT'
WHERE "IdSolicitudAporteRescate" = :IdSolicitudAporteRescate