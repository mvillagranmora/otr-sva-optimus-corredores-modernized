-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (120, 20, 1, 2025, 'T', '2025-03-10', 'Precomprobante 120', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (1301, 1, 120, '1101', 2025, 1, 10, 5, '2025-03-10', 'Ambos', 'V'),
 (1302, 1, 120, '2101', 2025, 2, 0, 0, '2025-03-10', 'Cero', 'V'),
 (1303, 1, 120, '4101', 2025, 3, 0, 7.5, '2025-03-10', 'Haber', 'V');
