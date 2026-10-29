SELECT 
	"IdCuadraturaDiariaOperaciones", 
	"IdEmpresa",
	"IdClasificacionTipoInstrumento",
	"OperacionCuadraturaDiariaOperaciones", 
	"FechaEjecucion", 
	"TotalOperaciones", 
	"OperacionesConProblemas", 
	"TipoOperacion", 
	"Resultado", 
	"Orden", 
	"DetalleCuadraturaJSON"
FROM "BSCL.Orden.BD::ORD.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;