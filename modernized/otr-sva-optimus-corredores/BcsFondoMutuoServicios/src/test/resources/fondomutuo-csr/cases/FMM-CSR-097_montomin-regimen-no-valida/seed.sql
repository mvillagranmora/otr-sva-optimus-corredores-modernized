-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "MontoMinimo" = 200000 WHERE "IdSerieFondo" = 'FM002A';
UPDATE "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" SET "ValidaMontoMinimoInstrumento" = 'N' WHERE "IdRegimenFondoMutuo" = 'GEN';
