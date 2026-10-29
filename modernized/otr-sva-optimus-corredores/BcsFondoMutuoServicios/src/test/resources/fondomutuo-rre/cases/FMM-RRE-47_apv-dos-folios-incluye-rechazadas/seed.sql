-- case seed on top of seed/base-fmm.sql + seed/base-rre.sql (fake data)
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ("IdPortafolio", "IdMatrizAgrupadorAPV") VALUES (500, 1);
INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("IdMatrizAgrupadorAPV", "IdRegimenTributario") VALUES (1, 10);
INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo") VALUES (10, 'APV-A');
