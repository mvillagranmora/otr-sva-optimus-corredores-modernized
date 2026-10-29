-- case seed on top of seed/base-fmm.sql + seed/base-rre.sql (fake data)
INSERT INTO "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" ("IdSerieFondo", "IdRegimenFondoMutuo", "IdEmpresa", "IdEstado") SELECT "IdSerieFondo", 'RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR', "IdEmpresa", "IdEstado" FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" WHERE "IdSerieFondo" = 'FM001A';
