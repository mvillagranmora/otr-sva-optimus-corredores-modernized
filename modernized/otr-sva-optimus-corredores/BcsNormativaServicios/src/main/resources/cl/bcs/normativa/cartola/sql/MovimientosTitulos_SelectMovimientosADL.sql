SELECT 
	SAR."IdSolicitudAporteRescate" AS "FolioComprobante",
	SAR."Overnight",
	SAR."AdministracionLiquidez"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" PP ON PP."IdPortafolio" = SAR."IdPortafolio"
WHERE  (SAR."FechaLiquidacion" BETWEEN :FechaDesde AND :FechaHasta
OR SAR."FechaCertificacion" BETWEEN :FechaDesde AND :FechaHasta
OR SAR."FechaIngresoRegistro" BETWEEN :FechaDesde AND :FechaHasta)
AND SAR."Overnight" = 'S'
AND SAR."AdministracionLiquidez" = 'S'
AND SAR."Estado" = 'CRT'