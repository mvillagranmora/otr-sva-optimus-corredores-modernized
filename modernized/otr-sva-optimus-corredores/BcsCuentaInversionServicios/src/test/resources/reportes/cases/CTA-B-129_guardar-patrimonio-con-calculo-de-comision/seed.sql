DELETE FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion" WHERE "IdMoneda" <> 'CLP';
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" VALUES (9201, 1001, 2001, DATE '2025-03-14', 'LIQ', 'CLP', 'CLP', 77, 77, 0, 0, 0, 0, 0, 'ACT', 0, 0, 0, 'INA');
INSERT INTO "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" VALUES (9202, 1001, 2001, DATE '2025-03-12', 'LIQ', 'CLP', 'CLP', 88, 88, 0, 0, 0, 0, 0, 'ACT', 0, 0, 0, 'INA');
UPDATE "BSCL.Plataforma.BD::PLA.Configuracion" SET "Valor" = 'S' WHERE "CodigoParametro" = 'CTA.CobroComisionPatrimonio';
INSERT INTO "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" VALUES (8260, 4001, 1, 'MOVABN', 'CRT', 'CTA', NULL, 'CNC001', 50000, DATE '2025-03-14', DATE '2025-03-14', TIMESTAMP '2025-03-14 10:00:00');
