SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.Custodia.BD::CUS.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa
  AND "FechaEjecucion" = :FechaEjecucion;
