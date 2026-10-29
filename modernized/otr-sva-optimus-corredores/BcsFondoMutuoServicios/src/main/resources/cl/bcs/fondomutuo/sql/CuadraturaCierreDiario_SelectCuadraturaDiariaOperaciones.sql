SELECT 
	"IdCuadraturaDiariaOperaciones", 
	"IdEmpresa",
	"OperacionCuadraturaDiariaOperaciones", 
	"FechaEjecucion", 
	"TotalOperaciones", 
	"OperacionesConProblemas", 
	"TipoOperacion", 
	"Resultado", 
	"Orden", 
	"DetalleCuadraturaJSON"
FROM "BSCL.FondoMutuo.BD::FMM.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;