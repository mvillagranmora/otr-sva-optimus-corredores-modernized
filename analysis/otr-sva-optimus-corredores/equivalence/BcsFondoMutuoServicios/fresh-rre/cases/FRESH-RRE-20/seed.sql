-- fresh seed
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "PatrimonioAltos" = 'S' WHERE "IdSerieFondo" = 'FM002A';
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = 'S' WHERE "NombreTipoParametro" = 'RESCATE_GENERA_VENTA_SPOT_DOLAR';
