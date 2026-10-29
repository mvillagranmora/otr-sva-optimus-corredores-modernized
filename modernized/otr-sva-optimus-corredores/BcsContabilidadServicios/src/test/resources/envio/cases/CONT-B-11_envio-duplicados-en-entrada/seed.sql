-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (1100, 20, 1, 2025, 'T', '2025-03-10', 'Precomprobante 1100', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V'),
 (1101, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 1101', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'C');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (11001, 1, 1100, '1101', 2025, 1, 10, 0, '2025-03-10', 'Dup', 'V'),
 (11002, 1, 1100, '2101', 2025, 2, 0, 10, '2025-03-10', 'Dup', 'V');
