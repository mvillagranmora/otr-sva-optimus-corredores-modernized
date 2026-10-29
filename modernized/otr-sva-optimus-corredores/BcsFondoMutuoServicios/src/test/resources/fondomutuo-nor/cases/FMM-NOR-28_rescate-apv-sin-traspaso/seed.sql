-- case seed on top of seed/base-fmm.sql (fake data)
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ("IdPortafolio", "IdMatrizAgrupadorAPV") VALUES (500, 7);
INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("IdMatrizAgrupadorAPV", "IdRegimenTributario") VALUES (7, 3);
INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo") VALUES (3, 'APV-A');
