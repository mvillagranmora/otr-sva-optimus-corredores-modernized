-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (304, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 304', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'E');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES
 (904, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES
 (7304, 904, '30400000-4', 1);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES
 (3041, 7304, 'VENTA', 'CLP', 10);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (30401, 1, 304, '1101', 2025, 1, 10, 0, '2025-03-10', 'E', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES
 (30401, 3041, '+', 1, 'V', 'Linea estado E');
