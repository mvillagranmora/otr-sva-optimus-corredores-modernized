-- fresh case (verify 2026-10-27, seed 20261027) on top of seed/base-fmm.sql + schema-cpf.sql + seed/base-cpf.sql
DELETE FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "IdEmpresa" = 1 AND "CodigoParametro" = 'PLA.CalculaValorCuotaDiaSiguiente';
INSERT INTO "BSCL.Plataforma.BD::PLA.Configuracion" ("IdEmpresa", "CodigoParametro", "Valor") VALUES (1, 'PLA.CalculaValorCuotaDiaSiguiente', 'S');
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (5001, 'INHABIL', DATE '0001-01-01', 5, 'dia inhabil', 2026, 56, 'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (5002, 'INHABIL', DATE '0001-01-01', 6, 'dia inhabil', 2026, 56, 'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (5003, 'FERIADO', DATE '2026-03-09', 0, 'feriado ficticio', 2026, 56, 'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (5004, 'FERIADO', DATE '2026-03-10', 0, 'feriado ficticio', 2026, 56, 'ACT');
