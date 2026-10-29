-- case seed on top of seed/base-fmm.sql + schema-cpf.sql + seed/base-cpf.sql (fake data)
UPDATE "BSCL.Plataforma.BD::PLA.MonedaFormato" SET "MontoTrucaRedondea" = 'T' WHERE "IdMoneda" = 'CLP';
