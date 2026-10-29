-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (300, 30, 1, 2025, 'T', '2025-03-10', 'Precomprobante 300', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'C'),
 (301, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 301', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'A'),
 (302, 20, 1, 2025, 'T', '2025-03-10', 'Precomprobante 302', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'D'),
 (303, 20, 1, 2025, 'T', '2025-03-11', 'Precomprobante 303', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'E');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (3021, 1, 302, '1101', 2025, 1, 2000, 0, '2025-03-10', 'Resumen 302', 'V'),
 (3022, 1, 302, '2101', 2025, 2, 0, 2000, '2025-03-10', 'Resumen 302', 'V'),
 (3031, 1, 303, '4101', 2025, 1, 300.25, 0, '2025-03-11', 'Resumen 303', 'V'),
 (3032, 1, 303, '1101', 2025, 2, 0, 300.25, '2025-03-11', 'Resumen 303', 'V'),
 (3033, 1, 303, '2101', 2025, 3, 0, 0, '2025-03-11', 'Linea en cero', 'V'),
 (3034, 1, 303, '9999', 2025, 4, 5, 0, '2025-03-11', 'Cuenta sin homologar', 'V');
