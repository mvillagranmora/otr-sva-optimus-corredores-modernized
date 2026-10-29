-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1700, 10, 1, 2025, 'T', '2025-08-01', 'Fresh 1700', '2025-08-01', '09:09:09', 'usr.f', 7, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (1907, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (17701, 1907, '77777777-7', 7);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (17001, 17701, 'VENTA', 'CLP', 100), (17002, 17701, 'VENTA', 'CLP', -100);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (17101, 1, 1700, '1201', 2025, 1, 100, 0, '2025-08-01', 'SYS cli', 'V'), (17102, 1, 1700, '4101', 2025, 2, 0, 100, '2025-08-01', 'SYS ing', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (17101, 17001, '+', 1, 'V', 'Venta sys'), (17102, 17002, '-', 2, 'V', 'Ingreso sys');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (17701, 'EsContraparte', 'NO', NULL, 'V', ''), (17701, 'CostingCode', 'CC-S', NULL, 'V', '');
