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
FROM "BSCL.CuentaInversion.BD::CTA.CuadraturaDiariaOperaciones"
WHERE "IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)
AND "IdEmpresa" = :IdEmpresa;