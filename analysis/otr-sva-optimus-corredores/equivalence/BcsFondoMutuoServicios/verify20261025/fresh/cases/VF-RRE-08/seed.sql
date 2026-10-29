-- fresh seed
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "MontoMinimo" = 5000000 WHERE "IdSerieFondo" = 'FM002A';
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "PatrimonioMinimo" = 1 WHERE "IdSerieFondo" = 'FM002A';
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "PatrimonioAltos" = 'S' WHERE "IdSerieFondo" = 'FM002A';
