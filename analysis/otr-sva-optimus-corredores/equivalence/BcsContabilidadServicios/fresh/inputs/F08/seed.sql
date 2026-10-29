-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1800, 10, 1, 2025, 'T', '2025-09-09', 'Fresh 1800', '2025-09-09', '19:00:00', 'usr.f', 8, NULL, 'E');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (1908, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (17801, 1908, '88888888-8', 8);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (18001, 17801, 'COMPRA', 'EUR', -42.42), (18002, 17801, 'COMPRA', 'EUR', 42.42);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (18101, 1, 1800, '1101', 2025, 1, 42.42, 0, '2025-09-09', 'EUR a', 'V'), (18102, 1, 1800, '1201', 2025, 2, 0, 42.42, '2025-09-09', 'EUR b', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (18101, 18001, '+', 1, 'V', 'Compra euros'), (18102, 18002, '-', 2, 'V', 'Compra euros cliente');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (17801, 'MontoCLP', NULL, 40000, 'V', ''), (17801, 'EsContraparte', 'NO', NULL, 'V', '');
