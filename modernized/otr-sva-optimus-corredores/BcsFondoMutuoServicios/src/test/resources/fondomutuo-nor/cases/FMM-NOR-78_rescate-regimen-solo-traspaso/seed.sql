-- case seed on top of seed/base-fmm.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" SET "PermiteRescate" = 'N', "PermiteTraspaso" = 'S' WHERE 1=1;
INSERT INTO "BSCL.APV.BD::TRA.Traspaso" ("IdTraspaso", "RazonRetiro", "IdPersona") VALUES (700, 'TRA', 1000);
