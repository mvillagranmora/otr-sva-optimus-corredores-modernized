-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1200, 20, 1, 2025, 'T', '2025-01-01', 'Fresh 1200', '2025-01-01', '00:00:00', 'usr.f', 1, NULL, 'D');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (12001, 1, 1200, '1101', 2025, 1, 0.01, 0, '2025-01-01', 'Un centavo', 'V'), (12002, 1, 1200, '2101', 2025, 2, 0, 0.01, '2025-01-01', 'Un centavo', 'V'), (12003, 1, 1200, '4101', 2025, 3, 3, 3, '2025-01-01', 'Ambos lados', 'V');
