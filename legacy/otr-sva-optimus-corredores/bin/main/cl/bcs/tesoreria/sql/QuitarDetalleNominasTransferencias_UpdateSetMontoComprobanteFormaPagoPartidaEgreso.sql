UPDATE CFP
SET CFP."Monto" = (
        SELECT NT."Monto"
        FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT
        WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia
    )
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP
INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT ON CFP."IdComprobante" = NT."IdComprobantePartidaEgreso"
WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia;
