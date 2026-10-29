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
FROM "BSCL.Mesa.BD::MES.CuadraturaDiariaOperacionesMRF"
WHERE "IdEmpresa" = :IdEmpresa;
 
 --"IdCuadraturaDiariaOperaciones" in (:IdCuadraturaDiariaOperaciones)