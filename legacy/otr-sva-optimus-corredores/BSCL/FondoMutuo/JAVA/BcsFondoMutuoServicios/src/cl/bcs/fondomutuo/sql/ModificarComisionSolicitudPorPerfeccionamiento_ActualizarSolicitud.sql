UPDATE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 
SET SOLICITUDES."MontoComision" = :MontoComision,
	SOLICITUDES."MontoComisionLocal" = :MontoComision,
	SOLICITUDES."MontoPago" = TO_DECIMAL(ROUND(SOLICITUDES."MontoPago" - :MontoComision,8),26,8),
	SOLICITUDES."MontoPagoLocal" = TO_DECIMAL(ROUND(SOLICITUDES."MontoPagoLocal" - :MontoComision,8),26,8),
	SOLICITUDES."ComisionInformada" = 'S'
WHERE "IdSolicitudAporteRescate" = :IdSolicitudAporteRescate