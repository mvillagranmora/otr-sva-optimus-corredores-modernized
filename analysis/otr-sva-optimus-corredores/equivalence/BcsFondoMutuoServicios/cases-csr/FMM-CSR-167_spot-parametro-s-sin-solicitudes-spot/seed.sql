-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'APORTE_GENERA_COMPRA_SPOT_DOLAR';
INSERT INTO "BSCL.FondoMutuo.BD::FMM.Parametro" ("IdEmpresa", "NombreTipoParametro", "ValorParametro") VALUES (1, 'APORTE_GENERA_COMPRA_SPOT_DOLAR', 'S');
