UPDATE C
SET C."Monto" = (
        SELECT NT."Monto"
        FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT
        WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia
    )
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS C
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON CFP."IdComprobantePartida" = C."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON CFP."IdComprobantePartida" = NT."IdComprobantePartidaEgreso"
WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia;
