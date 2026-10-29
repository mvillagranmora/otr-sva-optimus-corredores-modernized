SELECT 
	CAR."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoCar"
	,ClasificacionTipoInstrumento."Nombre" AS "NombreClasificacionTipoInstrumentoCar"
	,SUB."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoSub"
	,SUB."NombreClasificacionTipoInstrumento" AS "NombreClasificacionTipoInstrumentoSub"
	,CAR."IdTipoInstrumento" AS "IdTipoInstrumentoCar"
	,TipoInstrumento."Nombre" AS "NombreTipoInstrumentoCar"
	,SUB."IdTipoInstrumento" AS "IdTipoInstrumentoSub"
	,SUB."NombreTipoInstrumento" AS "NombreTipoInstrumentoSub"
	,SUM(CAR."INGMontoATasaMercadoActual") AS "MontoATasaCompraActualCar"
	,SUM(SUB."INGMontoATasaCompraActual") AS "MontoATasaCompraActualSub"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN (
	SELECT 
		SUB."FolioCarteraRelacionada"
		,SUB."IdTipoInstrumento"
		,SUB."IdClasificacionTipoInstrumento"
		,SUM(SUB."INGMontoMercadoActualMonedaInstrumento") AS "INGMontoATasaCompraActual"
		,ClasificacionTipoInstrumento."Nombre" AS "NombreClasificacionTipoInstrumento"
		,TipoInstrumento."Nombre" AS "NombreTipoInstrumento"
	FROM "BSCL.Custodia.BD::CUS.Subyacente" SUB
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TipoInstrumento ON SUB."IdTipoInstrumento" = TipoInstrumento."IdTipoInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ClasificacionTipoInstrumento ON SUB."IdClasificacionTipoInstrumento" = ClasificacionTipoInstrumento."IdClasificacionTipoInstrumento"
	WHERE SUB."IdEstado" = 'VIG'
	GROUP BY SUB."FolioCarteraRelacionada", SUB."IdTipoInstrumento", SUB."IdClasificacionTipoInstrumento", TipoInstrumento."Nombre", ClasificacionTipoInstrumento."Nombre"
) SUB 
	ON SUB."FolioCarteraRelacionada" = car."FolioCartera"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TipoInstrumento ON CAR."IdTipoInstrumento" = TipoInstrumento."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ClasificacionTipoInstrumento ON CAR."IdClasificacionTipoInstrumento" = ClasificacionTipoInstrumento."IdClasificacionTipoInstrumento"
WHERE 
	CAR."IdEmpresa" = :p_IdEmpresa 
	AND  COALESCE(:p_IdCliente, 0) IN (CAR."IdCliente", 0) --524891
	AND  COALESCE(:p_IdPortafolio, 0) IN (CAR."IdPortafolio", 0) 
	AND (CAR."FechaOperacion" <= :p_FechaConsulta) --'2021-04-13'
	AND CAR."IdEstado" = 'VIG'
GROUP BY
	CAR."IdClasificacionTipoInstrumento"
	,ClasificacionTipoInstrumento."Nombre"
	,SUB."IdClasificacionTipoInstrumento"
	,SUB."NombreClasificacionTipoInstrumento"
	,CAR."IdTipoInstrumento"
	,TipoInstrumento."Nombre"
	,SUB."IdTipoInstrumento"
	,SUB."NombreTipoInstrumento";