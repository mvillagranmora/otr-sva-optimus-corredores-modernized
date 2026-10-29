-- fresh seed
UPDATE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SET "MontoMinimo" = 5000000 WHERE "IdSerieFondo" = 'FMX02U';
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = 'S' WHERE "NombreTipoParametro" = 'RESCATE_GENERA_VENTA_SPOT_DOLAR';
