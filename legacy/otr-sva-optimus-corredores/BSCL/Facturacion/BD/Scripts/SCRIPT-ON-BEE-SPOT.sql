/*
 * UPSERT PARA HABILITAR BOLETA EXTENTA 
 * SELECT * FROM BSCL."BSCL.Facturacion.BD::FAC.TipoComprobante" WHERE "IdTipoComprobante"='BEE';
*/
SET SCHEMA "BSCL"; 

UPSERT BSCL."BSCL.Facturacion.BD::FAC.TipoComprobante"
VALUES ('BEE', 'BEE', 'BOLETA ELECTRONICA EXENTA', 'ACT', '2023-04-20 11:35:00.000', 'BOE', 'FOLBEE', 41, 'N', '', 'N', 'S')
WHERE "IdTipoComprobante"='BEE';