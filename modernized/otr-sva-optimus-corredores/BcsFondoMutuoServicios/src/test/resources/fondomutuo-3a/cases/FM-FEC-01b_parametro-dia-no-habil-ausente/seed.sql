DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro" WHERE "IdEmpresa" = 1 AND "NombreTipoParametro" = 'PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET';
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (5001, 'INHABIL', DATE '0001-01-01', 5, 'dia inhabil', 2026, 56, 'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (5002, 'INHABIL', DATE '0001-01-01', 6, 'dia inhabil', 2026, 56, 'ACT');
