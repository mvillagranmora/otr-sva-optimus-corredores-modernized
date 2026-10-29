SELECT cmbte."IdEstadoComprobante", cmbte."IdComprobante", mvtotran."IdMovimientoCarteraEnTransito", mvtotran."EsVentaDescubierta"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS mvtotran
 INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS cmbte 
   ON cmbte."IdComprobante" = mvtotran."FolioComprobante" 
WHERE mvtotran."IdMovimientoCarteraEnTransito" IN (:idsMovimientoCarteraEnTransito);  