SELECT COUNT(*) AS "Validacion" FROM "BSCL.Facturacion.BD::FAC.ConfiguracionAFP"
WHERE "IdAFP" = :IdAFP and "IdMultifondo" = :IdMultiFondo;