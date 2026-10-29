-- case seed on top of seed/base-fmm.sql + seed/base-ifm.sql (fake data)
DELETE FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" WHERE "IdSerieFondo" = 'S001';
INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" ("IdSerieFondo", "IdRegimenFondoMutuo", "IdEmpresa", "IdEstado") VALUES ('S001', '18T', 1, 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" ("IdSerieFondo", "IdRegimenFondoMutuo", "IdEmpresa", "IdEstado") VALUES ('S001', '18Q', 1, 'ACT');
