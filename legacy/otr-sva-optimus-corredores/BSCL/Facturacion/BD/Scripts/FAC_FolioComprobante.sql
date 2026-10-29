SET SCHEMA "BSCL"; 
 
DELETE FROM "BSCL.Facturacion.BD::FAC.FolioComprobante"; 
 
INSERT INTO "BSCL.Facturacion.BD::FAC.FolioComprobante"("IdFolioComprobante","Codigo","Nombre","FolioInicio","FolioFin","FolioActual","StockCritico")
VALUES('FOLFAC','FOLFAC','FOLIO FACTURA',1,999999999999,1,0);
INSERT INTO "BSCL.Facturacion.BD::FAC.FolioComprobante"("IdFolioComprobante","Codigo","Nombre","FolioInicio","FolioFin","FolioActual","StockCritico")
VALUES('FOLFAE','FOLFAE','FOLIO FACTURA ELECTRONICA',1,999999999999,1,0);
INSERT INTO "BSCL.Facturacion.BD::FAC.FolioComprobante"("IdFolioComprobante","Codigo","Nombre","FolioInicio","FolioFin","FolioActual","StockCritico")
VALUES('FOLFEE','FOLFEE','FOLIO FACTURA ELECTRONICA EXENTA',1,999999999999,1,0);
INSERT INTO "BSCL.Facturacion.BD::FAC.FolioComprobante"("IdFolioComprobante","Codigo","Nombre","FolioInicio","FolioFin","FolioActual","StockCritico")
VALUES('FOLNDC','FOLNDC','FOLIO NOTA DE CREDITO',1,999999999999,1,0);
INSERT INTO "BSCL.Facturacion.BD::FAC.FolioComprobante"("IdFolioComprobante","Codigo","Nombre","FolioInicio","FolioFin","FolioActual","StockCritico")
VALUES('FOLNDD','FOLNDD','FOLIO NOTA DE DEBITO',1,999999999999,1,0);



commit;
