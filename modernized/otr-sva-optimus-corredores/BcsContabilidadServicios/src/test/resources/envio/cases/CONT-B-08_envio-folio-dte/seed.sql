-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (800, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 800', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES
 (980, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES
 (7801, 980, '80000000-8', 21),
 (7802, 980, '80000001-6', 22),
 (7803, 980, '80000002-4', 23);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES
 (881, 7801, 'FACT', 'CLP', 10),
 (882, 7801, 'FACT', 'CLP', -10),
 (883, 7802, 'FACT', 'CLP', 20),
 (884, 7803, 'FACT', 'CLP', -20);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (80011, 1, 800, '1201', 2025, 1, 10, 0, '2025-03-10', 'F1', 'V'),
 (80012, 1, 800, '4101', 2025, 2, 0, 10, '2025-03-10', 'F1', 'V'),
 (80013, 1, 800, '1101', 2025, 3, 20, 0, '2025-03-10', 'F2', 'V'),
 (80014, 1, 800, '2101', 2025, 4, 0, 20, '2025-03-10', 'F3', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES
 (80011, 881, '+', 1, 'V', 'Factura servicios custodia'),
 (80012, 882, '-', 2, 'V', 'Ingreso custodia'),
 (80013, 883, '+', 3, 'V', 'Pago sin folio'),
 (80014, 884, '-', 4, 'V', 'Glosa de mas de cincuenta caracteres para probar el truncado final');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES
 (7801, 'FolioDTE', '45678', NULL, 'V', ''),
 (7802, 'FolioDTE', '0', NULL, 'V', ''),
 (7803, 'FolioDTE', '99', NULL, 'V', '');
