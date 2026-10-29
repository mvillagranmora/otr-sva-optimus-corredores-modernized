-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (2101, 10, 1, 2025, 'T', '2025-11-11', 'Fresh 2101', '2025-11-11', '11:11:11', 'usr.o', 21, NULL, 'V'), (2102, 20, 1, 2025, 'T', '2025-11-12', 'Fresh 2102', '2025-11-12', '12:12:12', 'usr.o', 22, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (2190, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (21901, 2190, '12121212-1', 12);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (21001, 21901, 'VENTA', 'CLP', 33), (21002, 21901, 'VENTA', 'CLP', -33);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (21011, 1, 2101, '1201', 2025, 1, 33, 0, '2025-11-11', 'X', 'V'), (21012, 1, 2101, '4101', 2025, 2, 0, 33, '2025-11-11', 'Y', 'V'), (21021, 1, 2102, '1101', 2025, 1, 4, 0, '2025-11-12', 'Z', 'V'), (21022, 1, 2102, '2101', 2025, 2, 0, 4, '2025-11-12', 'W', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (21011, 21001, '+', 1, 'V', 'rev x'), (21012, 21002, '-', 2, 'V', 'rev y');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (21901, 'EsContraparte', 'NO', NULL, 'V', '');
