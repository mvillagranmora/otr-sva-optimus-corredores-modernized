-- case seed on top of seed/base-fmm.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "MontoMinimo" = 500000 WHERE 1=1;
UPDATE "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" SET "ValidaMontoMinimoInstrumento" = 'N' WHERE 1=1;
