SELECT 
"IdCuadraturaCustodia",
"IdEmpresa",
"IdSerieFondo",
"TotalCuotasAGF",
"TotalCuotas",
"DiferenciaCuotas",
"ValorizacionCuotasAGF",
"ValorizacionCuotas",
"DiferenciaValorizacion",
TO_CHAR("FechaCuadratura",'YYYY-MM-DD') AS "FechaCuadratura",
"FechaIngresoRegistro",
"ValorCuota",
"IdRegimen",
"IdEstado",
"ISIN"
FROM "BSCL.FondoMutuo.BD::FMM.CuadraturaCustodia"
WHERE "IdEmpresa" = :IdEmpresa AND "FechaCuadratura" = :FechaCuadratura;