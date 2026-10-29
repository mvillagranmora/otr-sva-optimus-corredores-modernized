INSERT INTO "BSCL.Tesoreria.BD::TES.CuadraturaDiariaOperaciones"
(	"IdCuadraturaDiariaOperaciones",			"IdEmpresa", 			"OperacionCuadraturaDiariaOperaciones", 
	"FechaEjecucion", 							"TotalOperaciones", 	"OperacionesConProblemas", 					"TipoOperacion", 
	"Resultado", 								"Orden",				"DetalleCuadraturaJSON", "IdUsuario")
VALUES
(	:IdCuadraturaDiariaOperaciones,				:IdEmpresa,				:OperacionCuadraturaDiariaOperaciones,
	:FechaEjecucion,							:TotalOperaciones,		:OperacionesConProblemas,					:TipoOperacion,
	:Resultado,									:Orden,					:DetalleCuadraturaJSON, :IdUsuario
);