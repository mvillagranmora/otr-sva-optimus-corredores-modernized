
-- Inserta en tabla global parametros de entrada de movimientos de ingreso/egreso con los cuales generar comprobantes
INSERT INTO "BSCL.Tesoreria.BD::GBL.CrearComprobante_Entrada_IngresoEgresoGenerar" ("NumeroReferencia","IdIngresoEgreso","TipoComprobante")
VALUES (:NumeroReferencia,:IdIngresoEgreso,:TipoComprobante);
