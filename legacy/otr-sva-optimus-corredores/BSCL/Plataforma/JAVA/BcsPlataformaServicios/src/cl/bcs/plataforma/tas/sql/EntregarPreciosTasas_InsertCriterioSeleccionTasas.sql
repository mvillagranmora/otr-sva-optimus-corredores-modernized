/**************************************************************** 
Proposito	: Inserta criterios de seleccion de tasas
autor		: JVILLAGRAN - BCS
fecha		: 04/07/2018
*****************************************************************/

INSERT INTO "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_CriterioSeleccionTasas"
(
		"IdCriterioSeleccionTasas",		
		"Prioridad",
		"TipoCartera",
		"IdUso",
		"IdTipoCriterioTasa",
		"IdClasificacionTipoInstrumento",
		"IdTipoInstrumento",
		"DiasAntiguedad",
		"CodigoFuente",
		"IdTipoTasa",
		"IdTipoPrecio",
		"Interpolar",
		"IdEmpresa",
		"IdRegistro",		
		"FechaConsulta",		
		"FechaCriterioDesde",  
		"FechaCriterioHasta"		
)
VALUES
(
		:IdCriterioSeleccionTasas,		
		:Prioridad,
		:TipoCartera,
		:IdUso,
		:IdTipoCriterioTasa,
		:IdClasificacionTipoInstrumento,
		:IdTipoInstrumento,
		:DiasAntiguedad,		
		:CodigoFuente,
		:IdTipoTasa,
		:IdTipoPrecio,
		:Interpolar,
		:IdEmpresa,
		:IdRegistro,
		:FechaConsulta,		
		:FechaCriterioDesde,  -- Se calcula a partir de FechaConsulta y DiasAntiguedad  
		:FechaCriterioHasta   -- Es la misma que FechaConsulta
);