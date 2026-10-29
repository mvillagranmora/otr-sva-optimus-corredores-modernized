-- fresh case (verify 2026-10-27, seed 20261027) on top of seed/base-fmm.sql + schema-cpf.sql + seed/base-cpf.sql
INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" ("IdSerieFondo", "IdRegimenFondoMutuo", "IdEmpresa", "IdEstado") VALUES ('FM001A', 'BF', 1, 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" ("IdSerieFondo", "IdRegimenFondoMutuo", "IdEmpresa", "IdEstado") VALUES ('FM001A', '57BIS', 1, 'ACT');
