-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (2200, 20, 1, 2025, 'T', '2025-02-27', '', '2025-02-27', '07:00:00', 'usr.o', 23, NULL, 'D');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (22001, 1, 2200, '1101', 2025, 1, 50, 0, '2025-02-27', '', 'V'), (22002, 1, 2200, '2101', 2025, 2, 0, 50, '2025-02-27', '', 'V');
