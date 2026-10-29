UPDATE CD
SET CD."IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON CFP."IdComprobante" = CD."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON CFP."IdComprobantePartida" = NT."IdComprobantePartidaEgreso"
INNER JOIN #AnularNominasTransferencias_p_RegistrosAProcesar AS RP ON RP."Id" = NT."IdNominaTransferencia";