SELECT COUNT(*) AS "Validacion" FROM "BSCL.Facturacion.BD::FAC.ConfiguracionAFP"
WHERE "IdCliente" = :IdCliente AND "IdAFP" = :IdAFP;
