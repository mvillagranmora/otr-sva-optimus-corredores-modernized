SELECT CTA."IdCargoAbonoMovimiento"
	  ,'ANULACION POR COMPROBANTE TESORERIA ANULADO(' || CTA."IdComprobante" || ')' as "GlosaAnulacion"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS CTA
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON CFP."IdComprobante" =  CTA."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON NT."IdComprobantePartidaEgreso" = CFP."IdComprobantePartida"
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON COM."IdComprobante" = CFP."IdComprobante"
INNER JOIN #AnularNominasTransferencias_p_RegistrosAProcesar as RP ON RP."Id" = NT."IdNominaTransferencia"
WHERE CTA."IdModulo" = 'TES';