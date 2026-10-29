SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaInformeResultadoBuscar";

CREATE VIEW "BSCL.Normativa.BD.Vistas::ILS.ConsultaInformeResultadoBuscar"
AS 

SELECT 	TMP_ResultadoIndiceActual."IdTipoInforme"	
		,TMP_ResultadoIndiceActual."FechaContable"	
		,TMP_ResultadoIndiceActual."FechaEmision"	
		,TMP_ResultadoIndiceActual."HoraEmision"	
		,TMP_ResultadoIndiceActual."EstadoInforme"	
		,TMP_ResultadoIndiceActual."IdResultadoIndice" 
		,TMP_ResultadoIndiceActual."IdIndice"			
		,TMP_ResultadoIndiceActual."NombreIndice"	
		,TMP_ResultadoIndiceActual."MontoIndice"	
		,TMP_ResultadoIndiceActual."EstadoIndice"
		,COALESCE("InformeResultadoIndice"."IdResultadoIndice",0)			AS "IdResultadoIndiceAnterior"
		,COALESCE("InformeResultadoIndice"."FechaContable",'1900-01-01')	AS "FechaContableAnterior"
		,COALESCE("InformeResultadoIndice"."Monto",0)						AS "MontoAnterior"
		,COALESCE("InformeResultadoIndice"."IdIndice",0)			 		AS "IdIndiceAnterior"
		,COALESCE("InformeResultadoIndice"."NombreIndice",'')		 		AS "NombreIndiceAnterior"
		,COALESCE("InformeResultadoIndice"."Estado",'')			 			AS "EstadoAnterior"
		FROM 
			(SELECT  
					 "InformeResultado"."IdTipoInforme"		
					,"InformeResultado"."FechaContable"		
					,"InformeResultado"."FechaEmision"		
					,"InformeResultado"."HoraEmision"		
					,"InformeResultado"."Estado"					AS "EstadoInforme"				
					,"InformeResultadoIndice"."IdResultadoIndice"				
					,"InformeResultadoIndice"."Monto"				AS "MontoIndice"			
					,"InformeResultadoIndice"."IdIndice"			
					,"InformeResultadoIndice"."NombreIndice"		
					,"InformeResultadoIndice"."Estado"				AS "EstadoIndice"
				FROM "BSCL.Normativa.BD::ILS.InformeTipo" as "InformeTipo"
				INNER JOIN "BSCL.Normativa.BD::ILS.InformeResultado" as "InformeResultado"
					ON "InformeTipo"."IdTipoInforme" = "InformeResultado"."IdTipoInforme"
				LEFT JOIN "BSCL.Normativa.BD::ILS.InformeResultadoIndice" as "InformeResultadoIndice"
					ON "InformeResultado"."IdTipoInforme" = "InformeResultadoIndice"."IdTipoInforme"	
						AND	"InformeResultado"."FechaContable" = "InformeResultadoIndice"."FechaContable"					
			/* WHERE "InformeResultado"."Estado" = 'VIG'
					AND ("InformeResultado"."IdTipoInforme"	= :p_IdTipoInforme OR :p_IdTipoInforme = 0)
					AND ("InformeResultado"."FechaContable"	= :p_FechaContable	 OR :p_FechaContable = '')
         			AND "InformeTipo"."IdEmpresa" = :p_IdEmpresa*/
			) AS  TMP_ResultadoIndiceActual
		LEFT JOIN 
			"BSCL.Normativa.BD::ILS.InformeResultadoIndice" as "InformeResultadoIndice"
		ON  TMP_ResultadoIndiceActual."IdTipoInforme" = "InformeResultadoIndice"."IdTipoInforme" AND
			TMP_ResultadoIndiceActual."IdIndice"	  = "InformeResultadoIndice"."IdIndice"	 AND
			"InformeResultadoIndice"."FechaContable"  = (SELECT 
															MAX(NOR_ilsInformeResultadoindiceSUB."FechaContable") 
														FROM "BSCL.Normativa.BD::ILS.InformeResultadoIndice" AS NOR_ilsInformeResultadoindiceSUB  
														WHERE TMP_ResultadoIndiceActual."IdTipoInforme"  = NOR_ilsInformeResultadoindiceSUB."IdTipoInforme" AND 
															  TMP_ResultadoIndiceActual."FechaContable" > NOR_ilsInformeResultadoindiceSUB."FechaContable" and
															  TMP_ResultadoIndiceActual."IdIndice"	 =   NOR_ilsInformeResultadoindiceSUB."IdIndice"
														)

		ORDER BY 
			TMP_ResultadoIndiceActual."IdIndice";
			
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::ILS.ConsultaInformeResultadoBuscar" TO BCS_DESARROLLADOR WITH GRANT OPTION; 