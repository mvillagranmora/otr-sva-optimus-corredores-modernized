UPDATE "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion" SET "Valor" = '1' WHERE "CodigoParametro" = 'DIASREPROCESOSALDOREPORTEANTIGUEDAD';
INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" VALUES (DATE '2025-03-12', 1001, 2001, 'CLP', 'G', -100.00);
INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" VALUES (DATE '2025-03-12', 1001, 2001, 'CLP', 'I', -50.00);
