-- fresh case (verify 2026-10-27, seed 20261027) on top of seed/base-fmm.sql + schema-cpf.sql + seed/base-cpf.sql
UPDATE "BSCL.Plataforma.BD::PLA.MonedaFormato" SET "MontoDecimales" = 2 WHERE "IdMoneda" = 'CLP';
