DELETE 
FROM "BSCL.Facturacion.BD::FAC.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" IN (:IdCuadraturaDiariaOperaciones) AND "FechaEjecucion" IN (:FechaEjecucion);
