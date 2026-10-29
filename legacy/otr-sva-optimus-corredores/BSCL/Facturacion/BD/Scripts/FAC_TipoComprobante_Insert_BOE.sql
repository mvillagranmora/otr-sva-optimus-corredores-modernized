SET SCHEMA "BSCL"; 

DELETE FROM BSCL."BSCL.Facturacion.BD::FAC.TipoComprobante"
WHERE "IdTipoComprobante"='BOE';

INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.TipoComprobante"
("IdTipoComprobante", "Codigo", "Nombre", "IdEstadoFacturacion", "FechaIngresoRegistro", "TipoComprobanteFacturacion", "IdFolioComprobante", "TipoComprobanteDTE", "AfectoImpuesto", "TipoComprobanteDTEPE", "UsarEnFacturaAPartirDeOtra", "UsarEnSpot")
VALUES('BOE', 'BOE', 'BOLETA ELECTRONICA', 'ACT', '2016-07-04 15:46:37.000', 'BOE', 'FOLBOE', 39, 'S', '', 'N', 'N');