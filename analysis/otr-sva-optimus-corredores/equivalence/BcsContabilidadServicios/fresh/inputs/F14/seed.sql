-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (2310, 20, 1, 2025, 'T', '2025-06-01', 'Fresh 2310', '2025-06-01', '06:06:06', 'usr.v5', 41, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (23101, 1, 2310, '1101', 2025, 1, 0, 0, '2025-06-01', 'Linea cero', 'V'), (23102, 1, 2310, '1201', 2025, 2, 1000000, 0, '2025-06-01', 'Debe grande', 'V'), (23103, 1, 2310, '2101', 2025, 3, 0, 999999.99, '2025-06-01', 'Haber', 'V'), (23104, 1, 2310, '4101', 2025, 4, 0, 0.01, '2025-06-01', 'Centavo', 'V');
