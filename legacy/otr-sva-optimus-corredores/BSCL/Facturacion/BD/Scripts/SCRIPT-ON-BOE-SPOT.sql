/*
 * UPSERT PARA HABILITAR BOLETA 
 * SELECT * FROM BSCL."BSCL.Facturacion.BD::FAC.TipoComprobante" WHERE "IdTipoComprobante"='BOE';
*/
SET SCHEMA "BSCL"; 

UPSERT BSCL."BSCL.Facturacion.BD::FAC.TipoComprobante"
VALUES('BOE', 'BOE', 'BOLETA ELECTRONICA', 'ACT', '2016-07-04 15:46:37.000', 'BOE', 'FOLBOE', 39, 'S', '', 'N', 'S')
WHERE "IdTipoComprobante"='BOE';
