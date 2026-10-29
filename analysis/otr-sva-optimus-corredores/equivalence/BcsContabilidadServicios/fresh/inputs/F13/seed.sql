-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1310, 10, 1, 2025, 'T', '2025-04-04', 'Fresh 1310', '2025-04-04', '04:04:04', 'usr.f', 31, NULL, 'V'), (1311, 20, 1, 2025, 'T', '2025-04-05', 'Fresh 1311', '2025-04-05', '05:05:05', 'usr.f', 32, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (1931, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (13101, 1931, '13131313-1', 13), (13102, 1931, '14141414-1', 14);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (31001, 13101, 'VENTA', 'CLP', 10), (31002, 13102, 'VENTA', 'CLP', 20), (31003, 13101, 'VENTA', 'CLP', -30);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (31101, 1, 1310, '1201', 2025, 1, 30, 0, '2025-04-04', 'Dos movs', 'V'), (31102, 1, 1310, '4101', 2025, 2, 0, 30, '2025-04-04', 'Ing', 'V'), (31111, 1, 1311, '1101', 2025, 1, 8, 0, '2025-04-05', 'Res', 'V'), (31112, 1, 1311, '1101', 2025, 2, 0, 8, '2025-04-05', 'Res', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (31101, 31001, '+', 1, 'V', 'mov a'), (31101, 31002, '+', 1, 'V', 'mov b'), (31102, 31003, '-', 2, 'V', 'ing');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (13101, 'EsContraparte', 'NO', NULL, 'V', ''), (13102, 'EsContraparte', 'SI', NULL, 'V', ''), (13101, 'Nemo', 'N13', NULL, 'V', '');
