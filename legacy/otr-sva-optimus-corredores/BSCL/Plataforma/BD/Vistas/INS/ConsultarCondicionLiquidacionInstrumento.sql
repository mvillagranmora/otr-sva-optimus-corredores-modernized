SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCondicionLiquidacionInstrumento";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarCondicionLiquidacionInstrumento" AS

	WITH Condicion AS (

		SELECT 
			RAIZ."IdCondicionLiquidacion"					AS "IdCondicionDeLiquidacion",
			COND."Codigo"									AS "Codigo",
			COND."Nombre"									AS "Nombre",
			COND."PlazoEnDias"								AS "PlazoEnDias",
			COALESCE (OPER."IdTipoOperacion",'') 			AS "IdTipoOperacion",
			TIPO."Accion" 									AS "AccionTipoOperacion" , 
			OPER."IdEstado" 								AS "EstadoTipoOperacionYCondicionDeLiquidacion",
			(( COND."Codigo" || ' - ' ) || COND."Nombre" ) 	AS "NombreLargo" ,
			COND."Orden"									AS "Orden",
			INST."IdInstrumento"							AS "IdInstrumento",
			INST."Nemotecnico"								AS "Nemotecnico"
		FROM  	   	"BSCL.Plataforma.BD::INS.Instrumento" 							AS INST 
		INNER JOIN 	"BSCL.Plataforma.BD::INS.Instrumento" 							AS RAIZ	ON (INST."IdInstrumento" = RAIZ."IdInstrumentoRaiz" AND INST."IdEstado" = RAIZ."IdEstado" AND INST."EstaInscrito" = RAIZ."EstaInscrito") 
		INNER JOIN  "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" 				AS COND	ON (COND."IdCondicionDeLiquidacion" = RAIZ."IdCondicionLiquidacion")
		INNER JOIN 	"BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" 	AS OPER	ON (OPER."IdCondicionDeLiquidacion" = INST."IdCondicionLiquidacion")
		INNER JOIN 	"BSCL.Plataforma.BD::INS.TipoOperacion" 						AS TIPO	ON (TIPO."IdTipoOperacion" = OPER."IdTipoOperacion" )		
		WHERE       
			INST."IdEstado" 				= 'ACT'	AND 
			INST."EstaInscrito" 			= 'S'  AND 
			INST."IdCondicionLiquidacion" 	!= ''   

	UNION ALL 

		SELECT 
			INST."IdCondicionLiquidacion"						AS "IdCondicionDeLiquidacion",
			COND."Codigo"										AS "Codigo",
			COND."Nombre"										AS "Nombre",
			COND."PlazoEnDias"									AS "PlazoEnDias",
			COALESCE (TIPO."IdTipoOperacion",'') 				AS "IdTipoOperacion",
			TIPO."Accion" 										AS "AccionTipoOperacion" , 
			OPER."IdEstado" 									AS "EstadoTipoOperacionYCondicionDeLiquidacion",
			(( COND."Codigo" || ' - ' ) || COND."Nombre" )		AS "NombreLargo",
			COND."Orden"										AS "Orden",
			INST."IdInstrumento"								AS "IdInstrumento",
			INST."Nemotecnico"									AS "Nemotecnico"		
		FROM  		"BSCL.Plataforma.BD::INS.Instrumento" 							AS INST 
		INNER JOIN 	"BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" 				AS COND ON (COND."IdCondicionDeLiquidacion" = INST."IdCondicionLiquidacion")
		INNER JOIN 	"BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" 	AS OPER ON (OPER."IdCondicionDeLiquidacion" = INST."IdCondicionLiquidacion")
		INNER  JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" 						AS TIPO ON (TIPO."IdTipoOperacion" = OPER."IdTipoOperacion" )	
		WHERE 
			INST."IdEstado" 				= 'ACT'AND  
			INST."EstaInscrito" 			= 'S'  AND
			INST."IdCondicionLiquidacion" 	!= ''  AND 
			INST."IdInstrumentoRaiz" 		= 0		
			
	UNION ALL 

		SELECT
			COND."IdCondicionDeLiquidacion"						AS "IdCondicionDeLiquidacion",
			COND."Codigo"										AS "Codigo",
			COND."Nombre"										AS "Nombre",
			COND."PlazoEnDias"									AS "PlazoEnDias",
			COALESCE (TIPO."IdTipoOperacion",'') 				AS "IdTipoOperacion",
			OPER."Accion" 										AS "AccionTipoOperacion", 
			TIPO."IdEstado" 									AS "EstadoTipoOperacionYCondicionDeLiquidacion",
			(( COND."Codigo" || ' - ' ) || COND."Nombre" )  	AS "NombreLargo" ,
			COND."Orden"										AS "Orden",
			INST."IdInstrumento"								AS "IdInstrumento",
			INST."Nemotecnico"									AS "Nemotecnico"
		FROM  		"BSCL.Plataforma.BD::INS.Instrumento" 							AS INST,
					"BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" 				AS COND 
		INNER JOIN 	"BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" 	AS TIPO ON (TIPO."IdCondicionDeLiquidacion" = COND."IdCondicionDeLiquidacion")
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" 							AS OPER ON (OPER."IdTipoOperacion" = TIPO."IdTipoOperacion" )	
					
		WHERE  NOT EXISTS (SELECT "IdInstrumentoRaiz" FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "IdInstrumentoRaiz" = "IdInstrumento")
		AND INST."IdCondicionLiquidacion" = '' 
		)
		
	SELECT 
		"IdCondicionDeLiquidacion",
		"Codigo",
		"Nombre",
		"PlazoEnDias",
		"IdTipoOperacion",
		"AccionTipoOperacion",
		"EstadoTipoOperacionYCondicionDeLiquidacion",
		"NombreLargo",
		"Orden",
		"IdInstrumento" ,
		"Nemotecnico"
	FROM Condicion;
		
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarCondicionLiquidacionInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  