-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1910, 10, 1, 2025, 'T', '2025-10-01', 'Fresh 1910', '2025-10-01', '01:02:03', 'usr.f', 9, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (1909, 200);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (17901, 1909, '99999999-9', 9);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (19001, 17901, 'VENTA', 'CLP', 0), (19002, 17901, 'VENTA', 'CLP', 5);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (19101, 1, 1910, '1201', 2025, 1, 0, 0, '2025-10-01', 'Cero', 'V'), (19102, 1, 1910, '9998', 2025, 2, 5, 0, '2025-10-01', 'Sin homologar', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (19101, 19001, '+', 1, 'V', 'Cero'), (19102, 19002, '+', 2, 'V', 'Sin homologar');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (17901, 'EsContraparte', 'NO', NULL, 'V', '');
