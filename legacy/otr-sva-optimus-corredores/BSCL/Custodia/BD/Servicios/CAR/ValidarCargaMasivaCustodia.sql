SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Custodia.BD.Servicios.CAR::ValidarCargaMasivaCustodia";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios.CAR::ValidarCargaMasivaCustodia"(	
	IN  p_CargaMasivaValidar         TABLE ("IdCargaMasiva"       BIGINT),  
	OUT p_out_CargaMasivaInvalida    TABLE ("IdCargaMasiva"       BIGINT),  
	OUT p_out_ErroresValidacion      TABLE ("IdCargaMasiva"			 BIGINT,
											"IdCargaMasivaDetalle"   BIGINT,
											"ObservacionValidacion"	 NVARCHAR(520))
	) 	
	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT;
	DECLARE v_MensajesValidacion	TABLE (
											"Codigo"		NVARCHAR(100),
											"Descripcion"	NVARCHAR(520)											
										  );

	p_out_CargaMasivaInvalida   = SELECT 0 AS "IdCargaMasiva" FROM dummy WHERE 1=2;
	p_out_ErroresValidacion		= SELECT 0 AS "IdCargaMasiva", 0 AS "IdCargaMasivaDetalle", '' AS "ObservacionValidacion" FROM dummy WHERE 1=2;	
	v_MensajesValidacion		= SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1=2;
	
	BEGIN	
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
		DECLARE v_TablaValidaciones    	TABLE (	"Cumple" 				BIGINT, 
												"CodigoError" 			NVARCHAR(40), 
											   	"CodigoDato" 			NVARCHAR(100), 
											   	"ValorDato" 			NVARCHAR(520),
											   	"IdCargaMasivaDetalle" 	BIGINT, 
											   	"IdCargaMasiva" 		BIGINT );
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaValidaciones    	= SELECT  0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdCargaMasivaDetalle", 0 AS "IdCargaMasiva"  FROM dummy WHERE (1=2);		

		--------------------------------------------   
		-- VALIDACIONES
		--------------------------------------------		

		-- Obtiene detalles de carga masiva a validar
		v_CargaMasivaDetalle = 
		SELECT
			CMV."IdCargaMasiva"			,
			CMD."IdCargaMasivaDetalle"	,
			CM."IdTipoCargaMasiva"		,
			CMD."DocumentoIdentidad" 	,
			CMD."CodigoPortafolio"		,
			CMD."Nemotecnico"			,
			CMD."Emisor"				,
			CMD."FechaVencimiento"		,
			CMD."Cantidad"				,
			CMD."Precio"				,
			CMD."Tasa"					,
			CMD."Monto"					,
			TCM."Codigo"
		FROM :p_CargaMasivaValidar 									AS CMV
			INNER JOIN "BSCL.Custodia.BD::CUS.CargaMasivaDetalle" 	AS CMD 
				ON (CMD."IdCargaMasiva" = CMV."IdCargaMasiva")
			INNER JOIN "BSCL.Custodia.BD::CUS.CargaMasiva" 			AS CM
				ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva")
			INNER JOIN "BSCL.Custodia.BD::CUS.TipoCargaMasiva" 		AS TCM
				ON (CMD."IdCargaMasiva" = TCM."IdTipoCargaMasiva");

		-- Valida que exista Instrumento
		v_TablaValidaciones = 
		SELECT
				CASE 
					WHEN COALESCE(TM."Nemotecnico",'') = '' 
						THEN 0
					WHEN COALESCE(TM."IdEstado",'') = 'INA'
					 	THEN 0
					ELSE  
						1
				END 																AS "Cumple",
					CASE 
					WHEN COALESCE(TM."Nemotecnico",'') = '' 
						THEN 'UI_INSTRUMENTO_NO_EXISTE'
					WHEN COALESCE(TM."IdEstado",'') = 'INA'
					 		THEN 'UI_INSTRUMENTO_INACTIVO'
					ELSE 
					 		''
				END 																AS "CodigoError",
				'Nemotecnico' 														AS "CodigoDato",
		       	CAST(CMD."Nemotecnico" as NVARCHAR(50)) 							AS "ValorDato",
		       	CMD."IdCargaMasivaDetalle" 											AS "IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"		       										AS "IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS TM 
				ON TM."Nemotecnico" = CMD."Nemotecnico"
			  AND TM."IdEstado" IN ('ACT','INA')
		UNION ALL 
		SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
		FROM :v_TablaValidaciones;


		-- Valida que exista Socio de Negocio
		v_TablaValidaciones = 
		SELECT  
			CASE 
				WHEN COALESCE(Cliente."DocumentoIdentidad",'') = '' 
					THEN 0
				 ELSE  
				 	1
			END 																AS "Cumple",
			CASE 
				WHEN COALESCE(Cliente."DocumentoIdentidad",'') = '' 
					THEN 'SOCIO_NEGOCIO_NO_EXISTE'
				 ELSE  
				 	''
			END 																AS "CodigoError",
			'DocumentoIdentidad' 												AS "CodigoDato",
	       	CMD."DocumentoIdentidad" 											AS "ValorDato",
	       	CMD."IdCargaMasivaDetalle"											AS "IdCargaMasivaDetalle",
			CMD."IdCargaMasiva"													AS "IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS Cliente
				ON CMD."DocumentoIdentidad" = Cliente."DocumentoIdentidad"
			INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"		AS Portafolio
				ON Cliente."IdCliente" = Portafolio."IdCliente"
			   AND CMD."CodigoPortafolio" = Portafolio."CodigoPortafolio"
		WHERE Cliente."IdEstado" = 'ACT'
		  AND Portafolio."IdEstado" = 'ACT'
		UNION ALL 
		SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
		FROM :v_TablaValidaciones;


		-- Valida PORTAFOLIO
		v_TablaValidaciones = 
		SELECT
			CASE 
				WHEN COALESCE(Portafolio."CodigoPortafolio",'') = '' 
					THEN 0
				 ELSE  
				 	1
			END 																AS "Cumple",
			CASE 
				WHEN COALESCE(Portafolio."CodigoPortafolio",'') = '' 
					THEN 'PORTAFOLIO_INVALIDO'
				 ELSE  
				 	''
			END 																AS "CodigoError",
			'CodigoPortafolio'	 												AS "CodigoDato",
		   	CMD."CodigoPortafolio"	 											AS "ValorDato",
		   	CMD."IdCargaMasivaDetalle"											AS "IdCargaMasivaDetalle",
			CMD."IdCargaMasiva"													AS "IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 	AS Cliente
				ON CMD."DocumentoIdentidad" = Cliente."DocumentoIdentidad"
			LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"		AS Portafolio
				ON Cliente."IdCliente" = Portafolio."IdCliente"
			   AND CMD."CodigoPortafolio" = Portafolio."CodigoPortafolio"
			   AND Portafolio."IdEstado" = 
			   		CASE -- GCMBISEGCU-1 Permite carga de custodia externa solo en portafolios bloqueados
			   			WHEN (CMD."IdTipoCargaMasiva" <> 13 AND CMD."IdTipoCargaMasiva" <> 14)
						THEN 'ACT'
						ELSE 'BLO'
					END
		WHERE Cliente."IdEstado" = 'ACT'
		UNION ALL 
		SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
		FROM :v_TablaValidaciones;


		--Valida Cantidad
		v_TablaValidaciones = 
		SELECT  
			CASE 
				WHEN COALESCE(CMD."Cantidad",0) = 0  AND CMD."Codigo" <> 'DIVIDENDO_EN_CUOTAS_FFMM'
					THEN 0
				 ELSE  
				 	1
			END 																AS "Cumple",
			CASE 
				WHEN COALESCE(CMD."Cantidad",0) = 0
					THEN 'UI_CANTIDAD_DEBE_SER_MAYOR_QUE_CERO'
				 ELSE  
				 	''
			END 																AS "CodigoError",
			'Cantidad'			 												AS "CodigoDato",
	       	CAST(CMD."Cantidad" AS NVARCHAR(520))								AS "ValorDato",
	       	CMD."IdCargaMasivaDetalle"											AS "IdCargaMasivaDetalle",
			CMD."IdCargaMasiva"													AS "IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		UNION ALL 
		SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
		FROM :v_TablaValidaciones;
		
		--Valida Precio / Tasa
		v_TablaValidaciones = 
		SELECT  
			CASE 
				WHEN ((CMD."IdTipoCargaMasiva" = 1) AND (COALESCE(CMD."Precio",0) = 0))
					THEN 0
				WHEN ((CMD."IdTipoCargaMasiva" = 2) AND (COALESCE(CMD."Precio",0) = 0) AND (COALESCE(CMD."Tasa",0) = 0))
					THEN 0
				 ELSE  
				 	1
			END 																AS "Cumple",
			CASE 
				WHEN ((CMD."IdTipoCargaMasiva" = 1) AND (COALESCE(CMD."Precio",0) = 0))
					THEN 'PRECIO_MENOR_IGUAL_A_CERO'
				WHEN ((CMD."IdTipoCargaMasiva" = 2) AND (COALESCE(CMD."Precio",0) = 0) AND (COALESCE(CMD."Tasa",0) = 0))
					THEN 'REGISTRO_DEBE_TENER_PRECIO_O_TASA'
				 ELSE  
				 	''
			END 																AS "CodigoError",
			'Precio'			 												AS "CodigoDato",
	       	CAST(CMD."Precio" AS NVARCHAR(520))									AS "ValorDato",
	       	CMD."IdCargaMasivaDetalle"											AS "IdCargaMasivaDetalle",
			CMD."IdCargaMasiva"													AS "IdCargaMasiva"
		FROM :v_CargaMasivaDetalle AS CMD
		UNION ALL 
		SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
		FROM :v_TablaValidaciones;
				
		-------------------------------------------------
		-- ANALISIS DE DATOS
		-----------------------------------------------	   	
	   	
		-- Cargas Masivas con registros invalidos
	    p_out_CargaMasivaInvalida =
		    SELECT "IdCargaMasiva"
		    FROM :v_TablaValidaciones
		    WHERE "Cumple" = 0
			GROUP BY "IdCargaMasiva";
	    
	    
	    v_TablaCodigosMensajes =
			SELECT 	"CodigoError" AS "CodigoMensaje"
				   ,"CodigoDato"
				   ,"ValorDato" 
			FROM :v_TablaValidaciones
			WHERE "Cumple" = 0;
			
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, v_MensajesValidacion);


		p_out_ErroresValidacion = 
			SELECT 
				TV."IdCargaMasiva",
				TV."IdCargaMasivaDetalle",
				(CASE
					WHEN COALESCE(TV."ValorDato",'') = '' 
						THEN MV."Descripcion"
					ELSE 
						MV."Descripcion" || ' (' || TV."ValorDato" || ')'
				END) 															AS "ObservacionValidacion"
			FROM :v_TablaValidaciones AS TV
				INNER JOIN :v_MensajesValidacion AS MV  
					ON (TV."CodigoError" = MV."Codigo")
			WHERE TV."Cumple" = 0;

    END;
	
END;
