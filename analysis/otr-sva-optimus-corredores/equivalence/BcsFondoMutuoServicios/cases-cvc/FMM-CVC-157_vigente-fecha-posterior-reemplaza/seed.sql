-- case seed on top of seed/base-fmm.sql + schema-cvc.sql + seed/base-cvc.sql (fake data)
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ValorCuota" ("IdSerieFondo", "FechaCierre", "ValorCuota", "IdMoneda", "FechaCarga", "FechaIngresoRegistro", "IdEmpresa", "Observacion") VALUES ('FM001A', '2026-03-01', 100, 'CLP', '2026-03-01', TIMESTAMP '2026-02-20 10:00:00', 1, 'previa');
