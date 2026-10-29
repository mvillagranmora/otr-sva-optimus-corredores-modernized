
/***************************************************************** 
Proposito	: Consultar criterios de selecion de tasas
autor		: - BCS
fecha		: 04/07/2018
******************************************************************/

SELECT
	 DISTINCT(CST."IdCriterioSeleccionTasas") AS "IdCriterioSeleccionTasas"	
	,CST."Prioridad"
	,CST."TipoCartera"
	,CST."IdUso"
	,CST."IdTipoCriterioTasa"
	,CST."IdClasificacionTipoInstrumento"
	,CST."IdTipoInstrumento"
	,CST."DiasAntiguedad"   
	,CST."CodigoFuente"
	,CST."IdTipoTasa"
	,CST."IdTipoPrecio"
	,CST."Interpolar"
	,CST."IdEmpresa"
	,GLO."IdRegistro"
	,GLO."FechaConsulta"
	,CAST(GLO."FechaConsulta" AS NVARCHAR(10)) AS "FechaCriterioDesde" -- Esta fecha se recalcula mas adelante de cuerdo a "DiasAntiguedad" (si es que el criterio lo amerita)
	,CAST(GLO."FechaConsulta" AS NVARCHAR(10)) AS "FechaCriterioHasta" 

FROM "BSCL.Plataforma.BD::TAS.CriterioSeleccionTasas" AS CST
INNER JOIN "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_DatosInstrumentosProceso" AS GLO
	ON ((CST."TipoCartera"			= GLO."TipoCartera" OR CST."TipoCartera"='0' OR GLO."TipoCartera"='0')	AND	-- '0'='TODOS'
		(CST."IdUso"				= GLO."IdUso") AND
		(CST."IdTipoCriterioTasa"	= GLO."IdTipoCriterioTasa" OR CST."IdTipoCriterioTasa"='0' OR GLO."IdTipoCriterioTasa"='0') AND -- '0'='TODOS'
		(CST."IdClasificacionTipoInstrumento" = GLO."IdClasificacionTipoInstrumento") AND
		(CST."IdTipoInstrumento"	= GLO."IdTipoInstrumento" OR CST."IdTipoInstrumento"='0' OR GLO."IdTipoInstrumento"='0')) -- '0'='TODOS'
		
WHERE    CST."IdEmpresa"  = :IdEmpresa
AND      CST."Borrado"	  = 'N'
ORDER BY GLO."IdRegistro" ASC,CST."Prioridad" ASC;
