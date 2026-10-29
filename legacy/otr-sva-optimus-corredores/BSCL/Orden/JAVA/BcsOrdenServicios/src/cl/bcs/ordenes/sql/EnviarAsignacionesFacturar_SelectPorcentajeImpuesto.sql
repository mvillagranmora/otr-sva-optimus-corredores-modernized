SELECT "IdCobro","ValorCobro","IdMoneda" AS "IdMonedaCobro"
FROM "BSCL.Plataforma.BD::COM.Cobro"
WHERE "IdCobro" = :IdCobro AND "IdEmpresa" = :IdEmpresa;
