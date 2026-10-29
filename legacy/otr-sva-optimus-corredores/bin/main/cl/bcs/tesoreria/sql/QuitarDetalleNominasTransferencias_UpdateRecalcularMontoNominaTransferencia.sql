UPDATE NT
SET NT."Monto" = (
        SELECT SUM(CFP."Monto")
        FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT
            INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" AS NTD ON NT."IdNominaTransferencia" = NTD."IdNominaTransferencia"
            INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON NTD."IdComprobanteFormaDePago" = CFP."IdComprobanteFormaDePago"
            WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia
    )
FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" AS NT
WHERE NT."IdNominaTransferencia" = :IdNominaTransferencia
