-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1400, 10, 1, 2025, 'T', '2025-03-31', 'Fresh 1400', '2025-03-31', '08:00:00', 'usr.f', 4, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (1902, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (17401, 1902, '44444444-4', 4);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (14001, 17401, 'GASTO', 'CLP', 12.34), (14002, 17401, 'GASTO', 'CLP', -12.34);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (14101, 1, 1400, '5101', 2025, 1, 12.34, 0, '2025-03-31', 'Gasto', 'V'), (14102, 1, 1400, '4101', 2025, 2, 0, 12.34, '2025-03-31', 'Ingreso', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (14101, 14001, '+', 1, 'V', 'Gasto bancario'), (14102, 14002, '-', 2, 'V', 'Ingreso comision');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (17401, 'EsContraparte', 'NO', NULL, 'V', ''), (17401, 'CostingCode', 'CC-Q', NULL, 'V', '');
