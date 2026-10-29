-- case seed on top of seed/base-fmm.sql + schema-cvc.sql + seed/base-cvc.sql (fake data)
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ALTER COLUMN "ISIN" SET DATA TYPE VARCHAR_IGNORECASE(40);
