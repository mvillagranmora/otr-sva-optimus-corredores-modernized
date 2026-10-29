-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1100, 10, 1, 2025, 'T', '2025-06-30', 'Fresh 1100', '2025-06-30', '23:59:59', 'usr.f', 77, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (1900, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (17001, 1900, '11111111-1', 1);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (11001, 17001, 'VENTA', 'CLP', 999999999999.99), (11002, 17001, 'VENTA', 'CLP', -999999999999.99);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (11101, 1, 1100, '1201', 2025, 1, 999999999999.99, 0, '2025-06-30', 'Linea maxima', 'V'), (11102, 1, 1100, '4101', 2025, 2, 0, 999999999999.99, '2025-06-30', 'Linea maxima', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (11101, 11001, '+', 1, 'V', 'GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG'), (11102, 11002, '-', 2, 'V', 'HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (17001, 'EsContraparte', 'NO', NULL, 'V', ''), (17001, 'Nemo', 'FRESH-1', NULL, 'V', ''), (17001, 'Bpl', '1', NULL, 'V', ''), (17001, 'FechaLiq', '20250701', NULL, 'V', ''), (17001, 'Referencia', 'R-F01', NULL, 'V', '');
