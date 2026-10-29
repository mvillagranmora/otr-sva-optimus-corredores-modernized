UPDATE IEFP
	SET "IdEstado" = 'ING' 
FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IEFP
INNER JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON IEFP."IdIngresoEgreso" = IE."IdIngresoEgreso"
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen" AS CDO ON IE."IdIngresoEgreso" = CDO."IdIngresoEgreso"
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD ON CDO."IdComprobanteDetalle" = CD."IdComprobanteDetalle"
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON CD."IdComprobante" = COM."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON COM."IdComprobante" = CFP."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON CFP."IdComprobantePartida" = NT."IdComprobantePartidaEgreso"
INNER JOIN #AnularNominasTransferencias_p_RegistrosAProcesar AS RP ON RP."Id" = NT."IdNominaTransferencia"
WHERE IEFP."IdEstado" NOT IN ('ANU', 'MOD');