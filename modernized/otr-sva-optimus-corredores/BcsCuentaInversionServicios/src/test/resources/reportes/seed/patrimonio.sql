-- Shared fixture of the cash patrimony / commission calculation cases (GuardarPatrimonioCaja, GuardarPatrimonioCajaCalculoComision).
-- Use together with clientes-reportes.sql (accounts) and NOT with cobro-comision.sql (it defines other commission options).
-- Portfolio 2001 is non APV with commission option 1 (3.65% a year = 0.01% a day), portfolio 2002 is APV with option 2 (7.30%).
-- Previous business day patrimony (Thursday 2025-03-13, ADQ): portfolio 2001 = 1,000,000, portfolio 2002 = 2,000,000 affected by commission.
-- System date (base seed) is Friday 2025-03-14; weekend days are non business days.
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" VALUES (2001, 'P001', 'Inversion General', 'N');
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" VALUES (2002, 'P002', 'Cartera Empresa', 'S');
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" VALUES (2003, 'P003', 'Ahorro', 'N');
INSERT INTO "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" VALUES (2001, 'COBROCUST', '1');
INSERT INTO "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" VALUES (2002, 'COBROCUST', '2');
INSERT INTO "BSCL.Plataforma.BD.Vistas::IND.UIConsultarIndicadoresPortafolio" VALUES (2003, 'COBROCUST', '9');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" VALUES (7101, 1, 'N', 'CI', 0.0365, 'ACT', DATE '2025-01-01', '2025-01-01', TIMESTAMP '2025-01-01 00:00:00');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CobroComisionOpcionesCobro" VALUES (7102, 2, 'S', 'CI', 0.0730, 'ACT', DATE '2025-01-01', '2025-01-01', TIMESTAMP '2025-01-01 00:00:00');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" VALUES (9101, 1001, 2001, DATE '2025-03-13', 'ADQ', 'CLP', 'CLP', 1000000, 1000000, 0, 0, 0, 0, 0, 'ACT', 1000000, 100, 0.0365, 'VIG');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" VALUES (9102, 1002, 2002, DATE '2025-03-13', 'ADQ', 'CLP', 'CLP', 2000000, 2000000, 0, 0, 0, 0, 0, 'ACT', 2000000, 400, 0.0730, 'VIG');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" VALUES (9103, 1001, 2001, DATE '2025-03-13', 'LIQ', 'CLP', 'CLP', 5, 5, 0, 0, 0, 0, 0, 'ACT', 5, 0, 0, 'INA');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCaja" VALUES ('SaldoDisponible', '+', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCaja" VALUES ('SaldoEnGarantia', '+', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCaja" VALUES ('SaldoRetiro', '-', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCaja" VALUES ('SaldoInexistente', '+', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCaja" VALUES ('SaldoRetenido', '*', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionPatrimonioCaja" VALUES ('SaldoAPlazoSimultanea', '+', 'INA');
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" VALUES (1, 'CTA.CobroComisionPatrimonio', 'N');
