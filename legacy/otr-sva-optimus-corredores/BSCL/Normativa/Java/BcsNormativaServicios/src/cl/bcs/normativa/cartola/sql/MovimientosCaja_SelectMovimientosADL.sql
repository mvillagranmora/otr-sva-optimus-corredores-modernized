SELECT 
	SAR."IdSolicitudAporteRescate",
	SAR."Overnight",
	SAR."AdministracionLiquidez",
	CCR."IdCargoAbonoMovimiento"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SAR
INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" CCR ON  SAR."IdSolicitudAporteRescate" = CCR."IdOperacion"
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" PP ON CCR."IdPortafolio" = Pp."IdPortafolio"
WHERE  (SAR."FechaLiquidacion" BETWEEN :FechaDesde AND :FechaHasta
OR SAR."FechaCertificacion" BETWEEN :FechaDesde AND :FechaHasta
OR SAR."FechaIngresoRegistro" BETWEEN :FechaDesde AND :FechaHasta)
AND SAR."Overnight" = 'S'
AND SAR."AdministracionLiquidez" = 'S'
AND SAR."Estado" = 'CRT'
UNION
SELECT
	SAR."IdSolicitudAporteRescate",
	SAR."Overnight",
	SAR."AdministracionLiquidez",
	MCS.SEC_MOVIMIENTO AS "IdCargoAbonoMovimiento"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" SAR
INNER JOIN "BSCL.Normativa.BD::CAR.MovimientosCajaSebra" MCS ON  SAR."IdSolicitudAporteRescate" = MCS.SEC_MOVIMIENTO
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" PP ON MCS."IdPortafolio" = Pp."IdPortafolio"
WHERE  (SAR."FechaLiquidacion" BETWEEN :FechaDesde AND :FechaHasta
OR SAR."FechaCertificacion" BETWEEN :FechaDesde AND :FechaHasta
OR SAR."FechaIngresoRegistro" BETWEEN :FechaDesde AND :FechaHasta)
AND SAR."Overnight" = 'S'
AND SAR."AdministracionLiquidez" = 'S'
AND SAR."Estado" = 'CRT'