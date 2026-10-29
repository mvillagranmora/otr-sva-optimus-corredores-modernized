-- case seed on top of seed/base-fmm.sql + seed/base-fsa.sql (fake data; verify 2026-10-27)
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado", "Tipo", "Fecha", "Dia", "DescripcionDia", "AgnoCarga", "IdPais", "IdEstado") VALUES (5703, 'FERIADO', DATE '2027-01-01', 0, 'feriado ficticio', 2027, 56, 'ACT');
