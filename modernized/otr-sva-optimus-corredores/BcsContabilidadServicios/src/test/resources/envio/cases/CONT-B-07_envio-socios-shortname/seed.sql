-- Case seed (synthetic data). Loaded after schema-envio.sql and seed/base-envio.sql.
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES
 (700, 10, 1, 2025, 'T', '2025-03-10', 'Precomprobante 700', '2025-03-10', '18:00:00', 'usr.original', 55, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES
 (970, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES
 (7701, 970, '11111111-1', 11),
 (7702, 970, '22222222-2', 12),
 (7703, 970, '33333333-3', 13),
 (7704, 970, '44444444-4', 14);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES
 (771, 7701, 'VENTA', 'CLP', 100),
 (772, 7702, 'VENTA', 'CLP', -100),
 (773, 7703, 'VENTA', 'CLP', 50),
 (774, 7704, 'VENTA', 'CLP', -50);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES
 (70011, 1, 700, '1201', 2025, 1, 100, 0, '2025-03-10', 'Contraparte', 'V'),
 (70012, 1, 700, '2101', 2025, 2, 0, 100, '2025-03-10', 'Proveedor', 'V'),
 (70013, 1, 700, '1201', 2025, 3, 50, 0, '2025-03-10', 'Cliente', 'V'),
 (70014, 1, 700, '1101', 2025, 4, 0, 50, '2025-03-10', 'Contraparte no analitica', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES
 (70011, 771, '+', 1, 'V', 'Linea contraparte'),
 (70012, 772, '-', 2, 'V', 'Linea proveedor'),
 (70013, 773, '+', 3, 'V', 'Linea cliente'),
 (70014, 774, '-', 4, 'V', 'Linea banco');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES
 (7701, 'EsContraparte', 'SI', NULL, 'V', ''),
 (7702, 'EsContraparte', 'NO', NULL, 'V', ''),
 (7702, 'EsProveedor', 'SI', NULL, 'V', ''),
 (7704, 'EsContraparte', 'SI', NULL, 'V', '');
