-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (100, 20, 1, 2025, 'T', '2025-03-10', 'Precomprobante 100', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (1001, 1, 100, '1101', 2025, 1, 10, 0, '2025-03-10', 'X', 'V');
