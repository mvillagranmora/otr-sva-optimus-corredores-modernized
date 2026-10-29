INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'UI_ERROR_ALTOS_APORTE_FFMM', 'Aportes altos ::Monto:: fuera de ::Minimo:: a ::Maximo::');
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ("CodLenguaje", "CodDiccionario", "Descripcion") VALUES ('es', 'UI_ERROR_MONTO_TOTAL_INVERSION_FFMM', 'Total ::Monto:: fuera de ::Minimo:: a ::Maximo::');
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'MontoMinimoTotalInversion';
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'MontoMaximoTotalInversion';
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'MontoMinimoAportesAltos';
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'MontoMaximoAportesAltos';
