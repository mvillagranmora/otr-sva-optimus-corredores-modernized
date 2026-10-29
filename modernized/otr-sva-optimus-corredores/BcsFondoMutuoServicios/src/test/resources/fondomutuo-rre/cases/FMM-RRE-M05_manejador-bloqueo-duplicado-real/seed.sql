-- case seed on top of seed/base-fmm.sql + seed/base-rre.sql (fake data)
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion" ("IdBloqueaReinversion", "IdPortafolio", "IdRegimenTributario", "IdSerieFondo", "Monto", "NumeroCuotas", "FechaSistema", "IdCliente", "IdGrupoRegimen") VALUES ('500_GEN_FM002A_10_2026-03-02', 500, 'GEN', 'FM002A', 0, 10, '2026-03-02', 0, '');
