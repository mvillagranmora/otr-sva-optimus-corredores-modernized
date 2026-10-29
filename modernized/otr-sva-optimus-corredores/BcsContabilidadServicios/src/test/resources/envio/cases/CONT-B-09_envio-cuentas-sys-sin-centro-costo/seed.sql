-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (900, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 900', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES
 (990, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES
 (7901, 990, '90000000-9', 31);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES
 (991, 7901, 'VENTA', 'CLP', 75),
 (992, 7901, 'VENTA', 'CLP', -75);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (90011, 1, 900, '1201', 2025, 1, 75, 0, '2025-03-10', 'SYS', 'V'),
 (90012, 1, 900, '4101', 2025, 2, 0, 75, '2025-03-10', 'SYS', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES
 (90011, 991, '+', 1, 'V', 'Cuenta sys debe'),
 (90012, 992, '-', 2, 'V', 'Cuenta sys haber');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES
 (7901, 'CostingCode', 'CC-Z', NULL, 'V', '');
