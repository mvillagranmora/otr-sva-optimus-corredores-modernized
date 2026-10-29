UPDATE CD
SET CD."IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON CD."IdComprobante" = NT."IdComprobantePartidaEgreso"
INNER JOIN #AnularNominasTransferencias_p_RegistrosAProcesar AS RP ON RP."Id" = NT."IdNominaTransferencia";
