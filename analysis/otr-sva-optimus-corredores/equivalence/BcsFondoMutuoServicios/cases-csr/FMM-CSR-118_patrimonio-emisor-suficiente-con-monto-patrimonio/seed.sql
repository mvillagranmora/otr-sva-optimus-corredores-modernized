-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "PatrimonioAltos" = 'S', "MontoPatrimonioEmisor" = 500000 WHERE "IdSerieFondo" = 'FM002A';
