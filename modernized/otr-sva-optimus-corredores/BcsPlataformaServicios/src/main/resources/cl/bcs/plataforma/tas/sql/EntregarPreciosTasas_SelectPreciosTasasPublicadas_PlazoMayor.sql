
-- Objetivo: Obtienen los Precios y Tasas Publicas para el Plazo Mayor

WITH PreciosTasasPublicadas as
(
	SELECT
		 row_number() over (partition by RNP."Nemotecnico", RNP."IdMoneda" ,PTP."FechaSolicitud" order by RNP."Nemotecnico",RNP."IdMoneda", PTP."FechaSolicitud" desc) AS row_num
		,RNP."IdRegistro"
		,RNP."IdClasificacionTipoInstrumento"
		,RNP."IdTipoInstrumento"
		,RNP."IdInstrumento"
		,RNP."Nemotecnico"
		,RNP."IdMoneda"
		,RNP."PlazoResidual"
		,PTP."IdPrecioTasa"
		,PTP."IdFuente"
		,PTP."IdTipoTasa"
		,PTP."Tasa"
		,PTP."TasaEstimada"
		,PTP."IdTipoPrecio"
		,PTP."Precio"
		,PTP."FechaSolicitud"
		,PTP."Plazo"
		,RNP."FechaConsulta"

	FROM "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_DatosInstrumentosProceso" AS RNP	
	INNER JOIN "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_CriterioSeleccionTasas" AS CST ON (CST."IdRegistro" = RNP."IdRegistro")
	
	INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" AS PTP
		ON (      RNP."IdClasificacionTipoInstrumento" = PTP."IdClasificacionTipoInstrumento" 
		     AND  ((PTP."IdTipoTasa" IN ('CURVA','PUNTO') /*AND PTP."Nemotecnico" = ''*/) OR (RNP."IdTipoInstrumento" = PTP."IdTipoInstrumento" AND RNP."Nemotecnico" = PTP."Nemotecnico")) 
		     AND  RNP."IdMoneda"      = PTP."IdMoneda"
			 AND RNP."PlazoResidual" <= PTP."Plazo"
			 AND PTP."CodigoFuente"	  = CST."CodigoFuente"
			 AND (PTP."IdTipoTasa"    = CST."IdTipoTasa" OR PTP."IdTipoPrecio" = CST."IdTipoPrecio") 
			 AND PTP."IdEmpresa"	  = CST."IdEmpresa"
		   )
			 
	WHERE PTP."IdEstado"  = 'PUB'   -- Publicado
	AND   PTP."Borrado"   = 'N'
	AND   (TO_DATE(TO_VARCHAR(PTP."FechaSolicitud",'YYYY-MM-DD'),'YYYY-MM-DD') BETWEEN CST."FechaCriterioDesde" AND CST."FechaCriterioHasta")	  
	AND   CST."IdRegistro"   = :IdRegistro  
	AND   CST."CodigoFuente" = :CodigoFuente
)	


SELECT
		 PTP."IdRegistro", PTP."IdClasificacionTipoInstrumento", PTP."IdTipoInstrumento", PTP."IdInstrumento", PTP."Nemotecnico", PTP."IdMoneda", PTP."PlazoResidual", PTP."IdPrecioTasa"
		,PTP."IdFuente", PTP."IdTipoTasa", PTP."Tasa", PTP."TasaEstimada", PTP."IdTipoPrecio", PTP."Precio", PTP."FechaSolicitud", PTP."Plazo", PTP."FechaConsulta"
FROM PreciosTasasPublicadas AS PTP
INNER JOIN (
		SELECT "Nemotecnico", "IdMoneda", MIN("Plazo") AS "Plazo"   -- Se toma el mínimo de los mayores 
		FROM PreciosTasasPublicadas
		WHERE row_num = 1
		GROUP BY "Nemotecnico", "IdMoneda"
	) AS MINPLA ON (MINPLA."Nemotecnico"=PTP."Nemotecnico" AND MINPLA."IdMoneda"=PTP."IdMoneda" AND MINPLA."Plazo"= PTP."Plazo")									
WHERE row_num = 1;



