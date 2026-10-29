DELETE 
FROM "BSCL.Custodia.BD::CUS.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" IN (:IdCuadraturaDiariaOperaciones)
AND "FechaEjecucion" IN (:FechaEjecucion);
