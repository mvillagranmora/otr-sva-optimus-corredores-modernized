INSERT INTO "BSCL.Orden.BD::ORD.CuadraturaDiariaOperaciones"
(	"IdCuadraturaDiariaOperaciones",			"IdEmpresa", 			"IdClasificacionTipoInstrumento", 			"OperacionCuadraturaDiariaOperaciones", 
	"FechaEjecucion", 							"TotalOperaciones", 	"OperacionesConProblemas", 					"TipoOperacion", 
	"Resultado", 								"Orden", 				"DetalleCuadraturaJSON", "IdUsuario")
VALUES
(	:IdCuadraturaDiariaOperaciones,				:IdEmpresa,				:IdClasificacionTipoInstrumento,			:OperacionCuadraturaDiariaOperaciones,
	:FechaEjecucion,							:TotalOperaciones,		:OperacionesConProblemas,					:TipoOperacion,
	:Resultado,									:Orden,					:DetalleCuadraturaJSON, :IdUsuario
);