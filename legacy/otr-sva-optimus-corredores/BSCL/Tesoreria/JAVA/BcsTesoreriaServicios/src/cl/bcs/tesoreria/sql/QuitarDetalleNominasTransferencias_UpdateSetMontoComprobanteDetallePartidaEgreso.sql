UPDATE CD
SET CD."Monto" = (
        SELECT NT."Monto"
        FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT
        WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia
    )
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD
INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON CFP."IdComprobantePartida" = CD."IdComprobante"
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON CFP."IdComprobantePartida" = NT."IdComprobantePartidaEgreso"
WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia;
