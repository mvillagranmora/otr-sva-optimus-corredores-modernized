SELECT Mov."FolioComprobante" AS "IdCfact", Fac."Cantidad" AS "CantidadFactura", SUM(Mov."Cantidad") AS "CantidadMovimiento"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS Fac
LEFT JOIN  "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS Mov
ON Fac."IdComprobante" = Mov."FolioComprobante"
WHERE Mov."FolioComprobante" IN (:idComprobanteFacturacion)
GROUP BY Mov."FolioComprobante",Fac."Cantidad"