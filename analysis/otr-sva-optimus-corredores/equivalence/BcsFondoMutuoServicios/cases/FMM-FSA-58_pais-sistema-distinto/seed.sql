-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data)
DELETE FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = 1 AND "CodigoParametro" = 'PLA.PaisSistema';
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, 'PLA.PaisSistema', '840');
