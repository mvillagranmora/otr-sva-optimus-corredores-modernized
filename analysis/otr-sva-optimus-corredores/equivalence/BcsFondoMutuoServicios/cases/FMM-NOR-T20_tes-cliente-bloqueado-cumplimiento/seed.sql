-- case seed on top of seed/base-fmm.sql + schema-tesoreria-fm.sql + seed/base-tesoreria-fm.sql (fake data)
INSERT INTO "BSCL.Plataforma.BD::IND.TipoIndicador" ("IdTipoIndicador", "IdTipoEntidad", "CodigoTipoIndicador", "IdEstado") VALUES (1, 'CLIENTE', 'BCCUM', 'ACT');
UPDATE "BSCL.Plataforma.BD::IND.Indicador" SET "ValorIndicador" = 'S' WHERE "IdEntidad" = 100 AND "IdTipoIndicador" = 1;
