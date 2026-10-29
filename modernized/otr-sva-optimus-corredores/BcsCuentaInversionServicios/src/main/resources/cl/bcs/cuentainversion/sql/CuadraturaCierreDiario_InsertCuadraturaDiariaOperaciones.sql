INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuadraturaDiariaOperaciones"
(	"IdCuadraturaDiariaOperaciones",			"IdEmpresa", 			"OperacionCuadraturaDiariaOperaciones", 
	"FechaEjecucion", 							"TotalOperaciones", 	"OperacionesConProblemas", 					"TipoOperacion", 
	"Resultado", 								"Orden",				"DetalleCuadraturaJSON", "IdUsuario")
VALUES
(	:IdCuadraturaDiariaOperaciones,				:IdEmpresa,				:OperacionCuadraturaDiariaOperaciones,
	:FechaEjecucion,							:TotalOperaciones,		:OperacionesConProblemas,					:TipoOperacion,
	:Resultado,									:Orden,					:DetalleCuadraturaJSON, :IdUsuario
);