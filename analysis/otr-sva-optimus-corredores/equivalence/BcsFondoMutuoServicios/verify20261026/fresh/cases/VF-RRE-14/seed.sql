-- fresh seed
INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" ("IdPortafolio", "IdMatrizAgrupadorAPV") VALUES (500, 1);
INSERT INTO "BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario" ("IdMatrizAgrupadorAPV", "IdRegimenTributario") VALUES (1, 10);
INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario", "Codigo") VALUES (10, 'APV-A');
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = 'S' WHERE "NombreTipoParametro" = 'RESCATE_GENERA_VENTA_SPOT_DOLAR';
