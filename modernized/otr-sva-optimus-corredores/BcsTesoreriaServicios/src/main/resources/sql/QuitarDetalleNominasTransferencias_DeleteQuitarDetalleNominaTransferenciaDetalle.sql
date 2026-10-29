DELETE FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle"
WHERE "IdComprobanteFormaDePago" IN (
        SELECT CFP."IdComprobanteFormaDePago"
        FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
            INNER JOIN #QuitarDetalle_p_RegistrosAProcesar AS RP ON CFP."IdComprobante" = RP."IdComprobante"
            INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON NT."IdComprobantePartidaEgreso" = CFP."IdComprobantePartida"
                WHERE NT."IdNominaTransferencia" = RP."IdNominaTransferencia"
    );