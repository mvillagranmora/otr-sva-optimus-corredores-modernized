SELECT 
	"IdCuadraturaDiariaOperaciones", 
	"IdEmpresa",
	"IdTipoCentralizacion",
	"OperacionCuadraturaDiariaOperaciones", 
	"FechaEjecucion", 
	"TotalOperaciones", 
	"OperacionesConProblemas", 
	"TipoOperacion", 
	"Resultado", 
	"Orden", 
	"DetalleCuadraturaJSON"
FROM "BSCL.Contabilidad.BD::ITZ.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
  AND "IdEmpresa" = :IdEmpresa;