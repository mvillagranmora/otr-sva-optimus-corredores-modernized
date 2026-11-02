-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "IdEstado" = 'INA' WHERE "IdSerieFondo" = 'FM002A';
