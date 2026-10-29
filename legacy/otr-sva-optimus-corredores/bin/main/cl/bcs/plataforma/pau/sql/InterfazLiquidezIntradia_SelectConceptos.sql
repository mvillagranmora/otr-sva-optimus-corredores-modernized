SELECT "Concepto", "CodigoOperacion"
FROM "BSCL.Plataforma.BD::PLA.CodigoInterfazLiquidezIntradia"
WHERE "CodigoOperacion" IN (:IdConceptos);
