INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'UI_ERROR_ALTOS_APORTE_FFMM', 'Aportes altos ::Monto:: fuera de ::Minimo:: a ::Maximo::');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'UI_ERROR_MONTO_TOTAL_INVERSION_FFMM', 'Total ::Monto:: fuera de ::Minimo:: a ::Maximo::');
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = '1000' WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'MontoMinimoTotalInversion';
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = '50000' WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'MontoMaximoTotalInversion';
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = '20000' WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'MontoMinimoAportesAltos';
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = '90000' WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'MontoMaximoAportesAltos';
