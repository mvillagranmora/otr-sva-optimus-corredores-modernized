-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (100, 10, 1, 2025, 'T', '2025-03-10', 'Venta cartera propia 100', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES
 (900, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES
 (7001, 900, '76123456-7', 4711);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES
 (501, 7001, 'VENTA', 'CLP', -1500.5),
 (502, 7001, 'VENTA', 'CLP', 1500.5);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (1001, 1, 100, '1201', 2025, 1, 1500.5, 0, '2025-03-10', 'Venta', 'V'),
 (1002, 1, 100, '4101', 2025, 2, 0, 1500.5, '2025-03-10', 'Comision', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES
 (1001, 501, '+', 1, 'V', 'Venta pactada cliente'),
 (1002, 502, '-', 2, 'V', 'Comision venta');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES
 (7001, 'EsContraparte', 'NO', NULL, 'V', ''),
 (7001, 'Nemo', 'BCS-ACC', NULL, 'V', '');
