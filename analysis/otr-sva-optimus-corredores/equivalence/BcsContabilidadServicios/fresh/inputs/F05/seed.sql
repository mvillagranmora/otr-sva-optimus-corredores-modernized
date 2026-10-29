-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1501, 30, 1, 2025, 'T', '2025-05-05', 'Fresh 1501 inactivo', '2025-05-05', '10:00:00', 'usr.f', 5, NULL, 'V'), (1502, 20, 1, 2025, 'T', '2025-05-06', 'Fresh 1502', '2025-05-06', '10:00:00', 'usr.f', 5, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (15011, 1, 1501, '1101', 2025, 1, 10, 0, '2025-05-05', 'Inactivo', 'V'), (15021, 1, 1502, '1101', 2025, 1, 7, 0, '2025-05-06', 'R1', 'V'), (15022, 1, 1502, '2101', 2025, 2, 0, 7, '2025-05-06', 'R2', 'V');
