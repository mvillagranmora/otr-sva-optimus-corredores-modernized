UPDATE CFP
SET CFP."IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON CFP."IdComprobante" = NT."IdComprobantePartidaEgreso"
INNER JOIN #AnularNominasTransferencias_p_RegistrosAProcesar AS RP ON RP."Id" = NT."IdNominaTransferencia";
