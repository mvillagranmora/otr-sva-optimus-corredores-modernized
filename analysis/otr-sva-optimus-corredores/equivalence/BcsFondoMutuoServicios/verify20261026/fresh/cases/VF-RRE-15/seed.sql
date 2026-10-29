-- fresh seed
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ("IdPortafolio", "IdMatrizAgrupadorAPV") VALUES (500, 1);
INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("IdMatrizAgrupadorAPV", "IdRegimenTributario") VALUES (1, 10);
INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo") VALUES (10, 'APV-A');
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "MontoMinimo" = 1000 WHERE "IdSerieFondo" = 'FM001A';
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "PatrimonioMinimo" = 999999999 WHERE "IdSerieFondo" = 'FM002A';
