SELECT *
FROM (
		SELECT "ES"."Nombre"															   	AS "NombreEstado"
			  ,COUNT("CRD"."NombreEstado")												   	AS "RegistrosRecaudado"
			  ,TO_NUMBER(IFNULL(SUM("CRD"."DisTotal"),0)) 								   	AS "DisTribuidoTotal"
			  ,TO_NUMBER(IFNULL(SUM("CRD"."RecTotal"),0))								   	AS "RecTotal"
			  ,TO_NUMBER(IFNULL(SUM("CRD"."RecTotal"),0)- IFNULL(SUM("CRD"."DisTotal"),0)) 	AS "Diferencia"
			  ,'R'	AS "Grilla"
		FROM
		 "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido" AS "CRD"
		 RIGHT JOIN "BSCL.APV.BD::APV.Estado" AS "ES"
		 	ON "CRD"."CodigoEstado" = "ES"."Codigo"
		 		AND "CRD"."FechaDeposito" BETWEEN :p_FechaDesde AND :p_FechaHasta
		 		--AND "CRD"."FechaDeposito" BETWEEN  '2017-01-01' AND '2017-11-14'
		 	WHERE "NombreEntidad" = 'RECAUDACION'
		 	 	
		 GROUP BY "CodigoEstado","ES"."Nombre"
		 
		UNION ALL
		 
		SELECT 
			  'TOTALES'																			AS "NombreEstado",*
			  FROM(SELECT  COUNT("CRD"."NombreEstado")											AS "RegistrosRecaudado"
			  	  ,TO_NUMBER(IFNULL(SUM("CRD"."DisTotal"),0)) 									AS "DisTribuidoTotal"
				  ,TO_NUMBER(IFNULL(SUM("CRD"."RecTotal"),0))									AS "RecTotal"
				  ,TO_NUMBER(IFNULL(SUM("CRD"."RecTotal"),0)- IFNULL(SUM("CRD"."DisTotal"),0)) 	AS "Diferencia"
				  ,'R'	AS "Grilla"
		FROM
		 "BSCL.APV.BD::APV.Estado" AS "ES"
		 LEFT JOIN  "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido" AS "CRD"
		 	ON "CRD"."CodigoEstado" = "ES"."Codigo"
		 	 	AND "CRD"."FechaDeposito" BETWEEN :p_FechaDesde AND :p_FechaHasta
		 	 	--AND "CRD"."FechaDeposito" BETWEEN  '2017-01-01' AND '2017-11-14'
		 	WHERE "NombreEntidad" = 'RECAUDACION' )
		 	/*SELECT GRILLA 2	*/	
		 UNION ALL
			
		 	SELECT 
			'Total $ DC'																  					   AS "NombreEstado",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'CONCILIADO' AND "CRD"."TipoRegimen" = 'DC' THEN "CRD"."RecMonto" ELSE 0 END),0)) AS "RegistrosRecaudado",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'DISTRIBUIDO' AND "CRD"."TipoRegimen" = 'DC'THEN "CRD"."DisMontoDC" ELSE 0 END),0)) AS "DisTribuidoTotal",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'RECUPERADO' AND "CRD"."TipoRegimen" = 'DC' THEN "CRD"."RecMonto" ELSE 0 END),0)) AS "RecTotal",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'REZAGO' AND "CRD"."TipoRegimen" = 'DC'     THEN "CRD"."RecMonto" ELSE 0 END),0)) AS "Diferencia",
			'T'	AS "Grilla"			
		FROM
		 "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido" AS "CRD"
		 RIGHT JOIN "BSCL.APV.BD::APV.Estado" AS "ES"
		 	ON "CRD"."CodigoEstado" = "ES"."Codigo"
		 		AND "CRD"."FechaDeposito" BETWEEN :p_FechaDesde AND :p_FechaHasta
		 		--AND "CRD"."FechaDeposito" BETWEEN  '2017-01-01' AND '2017-11-14'
		 	WHERE "NombreEntidad" = 'RECAUDACION'
		 
		 UNION ALL
		 
		 SELECT 
			'Total $ APV'																   AS "NombreEstado",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'CONCILIADO' AND "CRD"."TipoRegimen" = 'APV' THEN "CRD"."RecMonto" ELSE 0 END),0)) AS "RegistrosRecaudado",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'DISTRIBUIDO' AND "CRD"."TipoRegimen" = 'APV' THEN "CRD"."DisMontoAPV" ELSE 0 END),0)) AS "DisTribuidoTotal",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'RECUPERADO' AND "CRD"."TipoRegimen" = 'APV' THEN "CRD"."RecMonto" ELSE 0 END),0)) AS "RecTotal",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'REZAGO' AND "CRD"."TipoRegimen" = 'APV'     THEN "CRD"."RecMonto" ELSE 0 END),0)) AS "Diferencia",
			'T'	AS "Grilla"
		FROM
		 "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido" AS "CRD"
		 RIGHT JOIN "BSCL.APV.BD::APV.Estado" AS "ES"
		 	ON "CRD"."CodigoEstado" = "ES"."Codigo"
		 		AND "CRD"."FechaDeposito" BETWEEN :p_FechaDesde AND :p_FechaHasta
		 	WHERE "NombreEntidad" = 'RECAUDACION'
		 
		  UNION ALL
		 
		 SELECT 
			'TOTAL'																								AS "NombreEstado",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'CONCILIADO' THEN ("CRD"."RecMonto") ELSE 0 END),0)) AS "RegistrosRecaudado",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'DISTRIBUIDO'THEN ("CRD"."DisMontoDC"+"CRD"."DisMontoAPV") ELSE 0 END),0)) AS "DisTribuidoTotal",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'RECUPERADO' THEN ("CRD"."RecMonto") ELSE 0 END),0)) AS "RecTotal",
			TO_NUMBER(IFNULL(SUM(CASE WHEN "ES"."Nombre"= 'REZAGO'     THEN ("CRD"."RecMonto") ELSE 0 END),0)) AS "Diferencia",
			'T'	AS "Grilla"
		FROM
		 "BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido" AS "CRD"
		 RIGHT JOIN "BSCL.APV.BD::APV.Estado" AS "ES"
		 	ON "CRD"."CodigoEstado" = "ES"."Codigo"
		 		AND "CRD"."FechaDeposito" BETWEEN :p_FechaDesde AND :p_FechaHasta
		 	WHERE "NombreEntidad" = 'RECAUDACION'
)
WHERE "Grilla" = :Grilla;
 