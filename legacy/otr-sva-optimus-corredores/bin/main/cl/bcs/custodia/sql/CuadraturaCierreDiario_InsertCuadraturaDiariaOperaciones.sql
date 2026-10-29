INSERT INTO "BSCL.Custodia.BD::CUS.CuadraturaDiariaOperaciones"
(	"IdCuadraturaDiariaOperaciones",			"IdEmpresa", 			"OperacionCuadraturaDiariaOperaciones", 
	"FechaEjecucion", 							"TotalOperaciones", 	"OperacionesConProblemas", 					"TipoOperacion", 
	"Resultado", 								"Orden",				"DetalleCuadraturaJSON", "IdUsuario")
VALUES
(	:IdCuadraturaDiariaOperaciones,				:IdEmpresa,				:OperacionCuadraturaDiariaOperaciones,
	:FechaEjecucion,							:TotalOperaciones,		:OperacionesConProblemas,					:TipoOperacion,
	:Resultado,									:Orden,					:DetalleCuadraturaJSON, :IdUsuario
);