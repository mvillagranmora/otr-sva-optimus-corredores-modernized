INSERT INTO #REGISTROSAGRUPADOS (
	"p_Cliente",
	"p_Portafolio",
	"p_FechaDeLiquidacion",
	"p_IdClasificacionIntrumento",
	"p_IdContraparteGarantia" ,
	"p_IdMonedaInstrumento" ,
	"p_IdConcepto" ,
	"p_IdInstrumento",
	"p_IdOrigenGarantia",
	"p_Nemotecnico",
	"CantidadAgrupada",
	"CantidadARetirar",
	"MontoARetirar"	
	)
VALUES (
	:p_Cliente,
	:p_Portafolio,
	:p_FechaDeLiquidacion,
	:p_IdClasificacionIntrumento,
	:p_IdContraparteGarantia,
	:p_IdMonedaInstrumento,
	:p_IdConcepto,
	:p_IdInstrumento,
	:p_IdOrigenGarantia,
	:p_Nemotecnico,
	:CantidadAgrupada,
	:CantidadARetirar,
	:MontoARetirar
	);