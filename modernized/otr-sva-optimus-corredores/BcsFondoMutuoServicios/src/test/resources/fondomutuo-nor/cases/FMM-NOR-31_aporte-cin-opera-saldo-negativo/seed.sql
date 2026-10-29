-- case seed on top of seed/base-fmm.sql (fake data)
INSERT INTO "BSCL.Plataforma.BD::IND.TipoIndicador" ("IdTipoIndicador", "IdTipoEntidad", "CodigoTipoIndicador", "IdEstado") VALUES (1, 1, 'GEN_OCSNEG', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::IND.Indicador" ("IdEntidad", "IdTipoIndicador", "IdTipoEntidad", "ValorIndicador") VALUES (500, 1, 1, 'S');
