-- case seed on top of seed/base-fmm.sql + schema-tesoreria-fm.sql + seed/base-tesoreria-fm.sql (fake data)
UPDATE "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" SET "GeneraComprobanteAutomaticamente" = 'S' WHERE "IdConcepto" = 'APORFM';
