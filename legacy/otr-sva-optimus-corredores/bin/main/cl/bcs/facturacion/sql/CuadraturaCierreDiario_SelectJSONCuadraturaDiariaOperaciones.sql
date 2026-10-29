SELECT 
	"DetalleCuadraturaJSON"
FROM "BSCL.Facturacion.BD::FAC.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa
  AND "FechaEjecucion" = :FechaInicio