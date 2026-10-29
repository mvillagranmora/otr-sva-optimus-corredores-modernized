SET SCHEMA "BSCL"; 

DELETE FROM BSCL."BSCL.Facturacion.BD::FAC.TipoComprobante"
WHERE "IdTipoComprobante"='BEE';

INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.TipoComprobante"
("IdTipoComprobante", "Codigo", "Nombre", "IdEstadoFacturacion", "FechaIngresoRegistro", "TipoComprobanteFacturacion", "IdFolioComprobante", "TipoComprobanteDTE", "AfectoImpuesto", "TipoComprobanteDTEPE", "UsarEnFacturaAPartirDeOtra", "UsarEnSpot")
VALUES('BEE', 'BEE', 'BOLETA ELECTRONICA EXENTA', 'ACT', '2023-04-20 11:35:00.000', 'BOE', 'FOLBEE', 41, 'N', '', 'N', 'N');