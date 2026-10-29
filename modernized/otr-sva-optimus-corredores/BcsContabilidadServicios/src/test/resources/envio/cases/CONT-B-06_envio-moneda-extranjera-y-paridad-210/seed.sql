-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (600, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 600', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V'),
 (610, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 610', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES
 (960, 200),
 (961, 210);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES
 (7600, 960, '96000000-6', 3),
 (7610, 961, '96100000-1', 4);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES
 (601, 7600, 'COMPRA', 'USD', 1000.0),
 (602, 7600, 'COMPRA', 'USD', -1000.0),
 (611, 7610, 'PAGO', 'USD', 500.0),
 (612, 7610, 'PAGO', 'USD', -500.0);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (6001, 1, 600, '1101', 2025, 1, 1000, 0, '2025-03-10', 'Compra USD', 'V'),
 (6002, 1, 600, '5101', 2025, 2, 0, 1000, '2025-03-10', 'Compra USD', 'V'),
 (6101, 1, 610, '1101', 2025, 1, 500, 0, '2025-03-10', 'Pago USD', 'V'),
 (6102, 1, 610, '5101', 2025, 2, 0, 500, '2025-03-10', 'Pago USD', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES
 (6001, 601, '+', 1, 'V', 'Compra dolares'),
 (6002, 602, '-', 2, 'V', 'Compra dolares gasto'),
 (6101, 611, '+', 1, 'V', 'Pago tesoreria'),
 (6102, 612, '-', 2, 'V', 'Pago tesoreria gasto');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES
 (7600, 'MontoCLP', NULL, -950123.45, 'V', ''),
 (7600, 'CostingCode', 'CC-X', NULL, 'V', ''),
 (7610, 'MontoCLP', NULL, 475000, 'V', '');
