-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (2410, 10, 1, 2025, 'T', '2025-09-29', 'Fresh 2410', '2025-09-29', '22:22:22', 'usr.v5', 42, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (2490, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (24901, 2490, '24242424-2', 24);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (24001, 24901, 'VENTA', 'CLP', 70), (24002, 24901, 'VENTA', 'CLP', 30), (24003, 24901, 'VENTA', 'CLP', -100);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (24101, 1, 2410, '1201', 2025, 1, 100, 0, '2025-09-29', 'Dos eventos', 'V'), (24102, 1, 2410, '4101', 2025, 2, 0, 100, '2025-09-29', 'Ingreso', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (24101, 24001, '+', 1, 'V', 'ev a'), (24101, 24002, '+', 1, 'V', 'ev b'), (24102, 24003, '-', 2, 'V', 'ing');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (24901, 'EsContraparte', 'NO', NULL, 'V', '');
