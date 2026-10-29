UPDATE CFP
SET CFP."IdEstado" = 'ING',
    CFP."IdComprobantePartida" = NULL
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
    INNER JOIN #QuitarDetalle_p_RegistrosAProcesar AS RP ON CFP."IdComprobante" = RP."IdComprobante"
    INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON NT."IdComprobantePartidaEgreso" = CFP."IdComprobantePartida"
    WHERE NT."IdNominaTransferencia" = RP."IdNominaTransferencia";