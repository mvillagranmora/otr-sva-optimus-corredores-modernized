-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (2000, 20, 1, 2025, 'T', '2025-12-30', 'Fresh 2000', '2025-12-30', '18:00:00', 'usr.o', 20, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (20001, 1, 2000, '1101', 2025, 1, 0.01, 0, '2025-12-30', 'A', 'V'), (20002, 1, 2000, '2101', 2025, 2, 0, 999999999999.99, '2025-12-30', 'B', 'V'), (20003, 1, 2000, '4101', 2025, 3, 999999999999.98, 0, '2025-12-30', 'C', 'V');
