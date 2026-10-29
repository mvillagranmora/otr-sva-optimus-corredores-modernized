UPDATE IE
	SET  IE."IdEstado" = 'ING'
		,IE."IdUsuarioEmision"	= 0
		,IE."FechaHoraEmision"	= '0001-01-01 00:00:00'
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON IE."IdIngresoEgreso" = CDO."IdIngresoEgreso"
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD ON CDO."IdComprobanteDetalle" = CD."IdComprobanteDetalle"
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON CD."IdComprobante" = COM."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON COM."IdComprobante" = CFP."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON CFP."IdComprobantePartida" = NT."IdComprobantePartidaEgreso"
INNER JOIN #AnularNominasTransferencias_p_RegistrosAProcesar AS RP ON RP."Id" = NT."IdNominaTransferencia";