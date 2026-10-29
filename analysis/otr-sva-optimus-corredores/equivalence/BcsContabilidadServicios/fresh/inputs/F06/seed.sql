-- Fresh verify input (synthetic data).
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobante" VALUES (1600, 10, 1, 2025, 'T', '2025-07-15', 'Fresh 1600', '2025-07-15', '16:30:00', 'usr.f', 6, NULL, 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Centralizacion" VALUES (1906, 210);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Movimiento" VALUES (17601, 1906, '55555555-5', 5), (17602, 1906, '66666666-6', 6);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.Evento" VALUES (16001, 17601, 'PAGO', 'USD', 1.01), (16002, 17602, 'PAGO', 'USD', -1.01);
INSERT INTO "BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento" VALUES (16101, 1, 1600, '1101', 2025, 1, 1.01, 0, '2025-07-15', 'USD a', 'V'), (16102, 1, 1600, '5101', 2025, 2, 0, 1.01, '2025-07-15', 'USD b', 'V');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento" VALUES (16101, 16001, '+', 1, 'V', 'USD con clp'), (16102, 16002, '-', 2, 'V', 'USD sin clp');
INSERT INTO "BSCL.Contabilidad.BD::ITZ.MovimientoDato" VALUES (17601, 'MontoCLP', NULL, -960.5, 'V', '');
