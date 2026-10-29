-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data)
UPDATE "BSCL.FondoMutuo.BD::FMM.Fondo" SET "MoneyMarket" = 'S' WHERE "IdFondo" = '41';
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" SET "ValorParametro" = 'S' WHERE "NombreTipoParametro" = 'PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET';
