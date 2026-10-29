INSERT INTO "BSCL.Contabilidad.BD::ITZ.CuadraturaDiariaOperaciones"
(	"IdCuadraturaDiariaOperaciones",			"IdEmpresa", 			"IdTipoCentralizacion",						"OperacionCuadraturaDiariaOperaciones", 
	"FechaEjecucion", 							"TotalOperaciones", 	"OperacionesConProblemas", 					"TipoOperacion", 
	"Resultado", 								"Orden",				"DetalleCuadraturaJSON", "IdUsuario")
VALUES
(	:IdCuadraturaDiariaOperaciones,				:IdEmpresa,				:IdTipoCentralizacion,						:OperacionCuadraturaDiariaOperaciones,
	:FechaEjecucion,							:TotalOperaciones,		:OperacionesConProblemas,					:TipoOperacion,
	:Resultado,									:Orden,					:DetalleCuadraturaJSON, :IdUsuario
);