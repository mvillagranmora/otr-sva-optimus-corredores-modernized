-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (500, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 500', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V'),
 (501, 20, 1, 2025, 'T', '2025-03-10', 'Precomprobante 501', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES
 (950, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES
 (7500, 950, '50000000-5', 2);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES
 (5001, 7500, 'VENTA', 'CLP', 80),
 (5002, 7500, 'VENTA', 'CLP', -80);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (50001, 1, 500, '1101', 2025, 1, 80, 0, '2025-03-10', 'Det 500', 'V'),
 (50002, 1, 500, '2101', 2025, 2, 0, 80, '2025-03-10', 'Det 500', 'V'),
 (50101, 1, 501, '1101', 2025, 1, 40, 0, '2025-03-10', 'Res 501', 'V'),
 (50102, 1, 501, '4101', 2025, 2, 0, 40, '2025-03-10', 'Res 501', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES
 (50001, 5001, '+', 1, 'V', 'Detalle 500 debe'),
 (50002, 5002, '-', 2, 'V', 'Detalle 500 haber');
