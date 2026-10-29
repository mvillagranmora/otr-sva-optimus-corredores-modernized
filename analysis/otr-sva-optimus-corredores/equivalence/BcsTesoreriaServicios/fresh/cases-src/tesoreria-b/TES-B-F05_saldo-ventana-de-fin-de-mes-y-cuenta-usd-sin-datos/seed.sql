-- comprobantes forma de pago of account 136 (view rows): prior balance on 2025-03-28 (two rows, last one by hora wins),
-- two movements affecting balance on 2025-04-01, one in transit (AfectoSaldo N) and two projected (FechaAfectaSaldo 0001-01-01)
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" VALUES (1001, 501, 136, 'LIQ', 'LIQ', 'S', '2025-03-28', '2025-03-28 09:00:00', '2025-03-28', '2025-03-28 09:00:00', 100.00, 0, 100.00, 1000.00);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" VALUES (1002, 502, 136, 'LIQ', 'LIQ', 'S', '2025-03-28', '2025-03-28 15:00:00', '2025-03-28', '2025-03-28 15:00:00', 0, 40.00, -40.00, 960.00);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" VALUES (1003, 503, 136, 'LIQ', 'LIQ', 'S', '2025-03-31', '2025-04-01 10:00:00', '2025-03-31', '2025-04-01 10:00:00', 250.50, 0, 250.50, 1210.50);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" VALUES (1004, 504, 136, 'LIQ', 'LIQ', 'S', '2025-03-31', '2025-04-01 11:00:00', '2025-03-31', '2025-04-01 11:00:00', 0, 60.25, -60.25, 1150.25);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" VALUES (1005, 505, 136, 'PEN', 'PEN', 'N', '0001-01-01', '0001-01-01 00:00:00', '2025-03-31', '0001-01-01 00:00:00', 30.00, 0, 30.00, 0);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" VALUES (1006, 506, 136, 'PEN', 'PEN', 'N', '0001-01-01', '0001-01-01 00:00:00', '2025-04-02', '0001-01-01 00:00:00', 0, 10.00, -10.00, 0);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" VALUES (1007, 507, 136, 'PEN', 'PEN', 'N', '0001-01-01', '0001-01-01 00:00:00', '2025-04-03', '0001-01-01 00:00:00', 500.00, 0, 500.00, 0);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" VALUES (1008, 508, 136, 'ANU', 'ANU', 'N', '0001-01-01', '0001-01-01 00:00:00', '2025-04-02', '0001-01-01 00:00:00', 999.00, 0, 999.00, 0);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" VALUES (2001, 136, 'ING', 'ING', '2025-03-31', 0, 5.00, -5.00);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" VALUES (2002, 136, 'ING', 'ING', '2025-04-02', 7.00, 0, 7.00);
INSERT INTO "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoFormaDePago" VALUES (2003, 136, 'ANU', 'ING', '2025-04-02', 77.00, 0, 77.00);
