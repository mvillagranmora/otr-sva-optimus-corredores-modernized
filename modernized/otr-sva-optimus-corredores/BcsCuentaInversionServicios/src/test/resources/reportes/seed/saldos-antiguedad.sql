-- Shared fixture of the aging-report balance cases (ActualizarSaldos / CargaInicial / GuardarSaldos). Synthetic data.
-- Two clients (1001 with CLP and USD accounts, 1002 with a CLP account); system date 2025-03-14 (Friday, from base seed).
-- Last stored aging balances: 2025-03-10, client 1001 CLP: general -100.00, intermediation -50.00; client 1002 CLP: general 500.00, intermediation 0.
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" VALUES (101, '76000001-1');
INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" VALUES (102, '12345678-5');
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" VALUES (1001, 1, 101, 0);
INSERT INTO "BSCL.SocioNegocio.BD::CLI.Cliente" VALUES (1002, 1, 102, 0);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (2001, 1001, 'P001', 'Inversion General', 0);
INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" VALUES (2002, 1002, 'P002', 'Ahorro Previsional', 55);
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4001, 1001, 2001, 'CLP', 'ACT');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4002, 1001, 2001, 'USD', 'ACT');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion" VALUES (4003, 1002, 2002, 'CLP', 'ACT');
INSERT INTO "BSCL.Facturacion.BD::FAC.Comprobante" VALUES (6001, 'RV', 'T0');
INSERT INTO "BSCL.Facturacion.BD::FAC.Comprobante" VALUES (6002, 'RF', 'T0');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" VALUES (DATE '2025-03-10', 1001, 2001, 'CLP', 'G', -100.00);
INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" VALUES (DATE '2025-03-10', 1001, 2001, 'CLP', 'I', -50.00);
INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" VALUES (DATE '2025-03-10', 1002, 2002, 'CLP', 'G', 500.00);
INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" VALUES (DATE '2025-03-10', 1002, 2002, 'CLP', 'I', 0.00);
