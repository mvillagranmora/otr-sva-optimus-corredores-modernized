-- case seed on top of seed/base-fmm.sql + seed/base-vdo.sql (fake data; verify 2026-10-27)
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'UsaDolarObservado';
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Parametro" ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (1, 'UsaDolarObservado', 'S');
