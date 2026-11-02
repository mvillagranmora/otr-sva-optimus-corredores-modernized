-- case seed on top of seed/base-fmm.sql + seed/base-csr.sql (fake data)
DELETE FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionPropositoPortafolio" WHERE "IdRegimenFondoMutuo" = 'GEN';
