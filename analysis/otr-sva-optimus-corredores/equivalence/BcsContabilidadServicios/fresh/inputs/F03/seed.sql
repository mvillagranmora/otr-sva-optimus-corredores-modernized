-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1300, 10, 1, 2025, 'T', '2025-02-28', 'Fresh 1300', '2025-02-28', '12:00:00', 'usr.f', 3, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (1901, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (17301, 1901, '22222222-2', 2), (17302, 1901, '33333333-3', 3);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (13001, 17301, 'PAGO', 'CLP', 250), (13002, 17302, 'PAGO', 'CLP', -250);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (13101, 1, 1300, '1201', 2025, 1, 250, 0, '2025-02-28', 'Contraparte', 'V'), (13102, 1, 1300, '2101', 2025, 2, 0, 250, '2025-02-28', 'Proveedor', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (13101, 13001, '+', 1, 'V', 'Pago contraparte'), (13102, 13002, '-', 2, 'V', 'Pago proveedor');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (17301, 'EsContraparte', 'SI', NULL, 'V', ''), (17302, 'EsContraparte', 'NO', NULL, 'V', ''), (17302, 'EsProveedor', 'SI', NULL, 'V', ''), (17301, 'Nemo', 'N-3', NULL, 'V', '');
