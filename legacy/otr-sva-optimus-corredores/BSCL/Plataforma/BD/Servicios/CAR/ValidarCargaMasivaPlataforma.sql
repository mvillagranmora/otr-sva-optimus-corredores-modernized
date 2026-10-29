SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios.CAR::ValidarCargaMasivaPlataforma";
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios.CAR::ValidarCargaMasivaPlataforma"(	
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
		DECLARE v_IdTipo BIGINT;
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaValidaciones    	= SELECT  0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdCargaMasivaDetalle", 0 AS "IdCargaMasiva"  FROM dummy WHERE (1=2);		
		v_IdTipo	= 0;
		--------------------------------------------   
		-- VALIDACIONES
		--------------------------------------------		

		-- Obtiene detalles de carga masiva a validar
		v_CargaMasivaDetalle = 
		SELECT
			CMV."IdCargaMasiva"						,
			CM."IdTipoCargaMasiva"					,
			CMD."IdCargaMasivaDetalle"				,
			CMD."Nemotecnico"						,
			CMD."RF_PorcentajeDescuento"			,
			CMD."RV_IdEntidadClasificadoraRiesgo"	,
			CMD."RV_Valor"
		FROM :p_CargaMasivaValidar 									AS CMV
			INNER JOIN "BSCL.Plataforma.BD::PLA.CargaMasivaDetalle" 	AS CMD 
				ON (CMD."IdCargaMasiva" = CMV."IdCargaMasiva")
			INNER JOIN "BSCL.Plataforma.BD::PLA.CargaMasiva" 			AS CM
				ON (CMD."IdCargaMasiva" = CM."IdCargaMasiva");

		
		-- Valida que exista Instrumento
		v_TablaValidaciones = 
		SELECT
				CASE 
					WHEN COALESCE(TM."Nemotecnico",'') = '' 
						THEN 0
					ELSE  
						1
				END 																AS "Cumple",
				CASE 
					WHEN COALESCE(TM."Nemotecnico",'') = '' 
						THEN 'UI_INSTRUMENTO_NO_EXISTE'
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
		UNION ALL 
		SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
		FROM :v_TablaValidaciones;
	
				
		SELECT TOP 1 "IdTipoCargaMasiva" INTO v_IdTipo 
		FROM :v_CargaMasivaDetalle;
		
		
		IF :v_IdTipo = 1 THEN --PORCENTAJE_DESCUENTO_RF
			-- Valida que el porcentaje sea entre 0 y 100.
			v_TablaValidaciones = 
			SELECT
					CASE 
						WHEN COALESCE(CMD."RF_PorcentajeDescuento",0) < 0 OR COALESCE(CMD."RF_PorcentajeDescuento",0) > 100 
						THEN	0	
						ELSE	1	
					END																	AS "Cumple",
					CASE 
						WHEN COALESCE(CMD."RF_PorcentajeDescuento",0) < 0 OR COALESCE(CMD."RF_PorcentajeDescuento",0) > 100 
						THEN	'UI_PORCENTAJE_ENTRE_0_100'	
						ELSE	''	
					END																	AS "CodigoError",
					'RF_PorcentajeDescuento' 											AS "CodigoDato",
			       	CAST(CMD."RF_PorcentajeDescuento" as NVARCHAR(50)) 					AS "ValorDato",
			       	CMD."IdCargaMasivaDetalle" 											AS "IdCargaMasivaDetalle",
					CMD."IdCargaMasiva"		       										AS "IdCargaMasiva"
			FROM :v_CargaMasivaDetalle AS CMD
			UNION ALL 
			SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
			FROM :v_TablaValidaciones;
	
		ELSEIF :v_IdTipo = 2 THEN --VALOR_RIESGO
			-- RV_Valor debe ser A, B, C, D.
			v_TablaValidaciones = 
			SELECT
					CASE 
						WHEN COALESCE(CMD."RV_Valor",'') != 'A' AND  COALESCE(CMD."RV_Valor",'') != 'B' AND COALESCE(CMD."RV_Valor",'') != 'C' AND COALESCE(CMD."RV_Valor",'') != 'D'  
						THEN	0	
						ELSE	1	
					END																	AS "Cumple",
					CASE 
						WHEN COALESCE(CMD."RV_Valor",'') != 'A' AND  COALESCE(CMD."RV_Valor",'') != 'B' AND COALESCE(CMD."RV_Valor",'') != 'C' AND COALESCE(CMD."RV_Valor",'') != 'D'  
						THEN	'UI_VALOR_DEBE_SER_A_B_C_D'	
						ELSE	''	
					END																	AS "CodigoError",
					'RV_Valor' 															AS "CodigoDato",
			       	CAST(CMD."RV_Valor" as NVARCHAR(50)) 								AS "ValorDato",
			       	CMD."IdCargaMasivaDetalle" 											AS "IdCargaMasivaDetalle",
					CMD."IdCargaMasiva"		       										AS "IdCargaMasiva"
			FROM :v_CargaMasivaDetalle AS CMD
			UNION ALL 
			SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
			FROM :v_TablaValidaciones;
			
--	 		--Valida Entidad
			v_TablaValidaciones = 
			SELECT
				CASE 
					WHEN COALESCE(VCR."IdEntidadClasificadoraRiesgo",'') = '' 
						THEN 0
					 ELSE  
					 	1
				END 																AS "Cumple",
				CASE 
					WHEN COALESCE(VCR."IdEntidadClasificadoraRiesgo",'') = '' 
						THEN 'UI_ENTIDAD_NO_EXISTE'
					 ELSE  
					 	''
				END 																AS "CodigoError",
				'IdEntidadClasificadoraRiesgo'			 							AS "CodigoDato",
			   	CMD."RV_IdEntidadClasificadoraRiesgo"									AS "ValorDato",
			   	CMD."IdCargaMasivaDetalle"											AS "IdCargaMasivaDetalle",
				CMD."IdCargaMasiva"													AS "IdCargaMasiva"
			FROM :v_CargaMasivaDetalle AS CMD
			LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" VCR
			ON VCR."IdEntidadClasificadoraRiesgo" = CMD."RV_IdEntidadClasificadoraRiesgo"
			UNION ALL 
			SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
			FROM :v_TablaValidaciones;
			
--			--Valida Entidad
--			v_TablaValidaciones = 
--			SELECT
--				CASE 
--					WHEN COALESCE(VCR."IdEntidadClasificadoraRiesgo",'') = '' 
--					THEN 0
--					ELSE  
--					 	1
--					END 															AS "Cumple",
--				CASE 
--					WHEN COALESCE(VCR."IdEntidadClasificadoraRiesgo",'') = '' 
--					THEN 'UI_INSTRUMENTO_ENTIDAD_NO_RELACIONADOS'
--					ELSE  
--						''
--					END 															AS "CodigoError",
--				'IdEntidadClasificadoraRiesgo'			 							AS "CodigoDato",
--			   	CMD."RV_IdEntidadClasificadoraRiesgo"								AS "ValorDato",
--			   	CMD."IdCargaMasivaDetalle"											AS "IdCargaMasivaDetalle",
--				CMD."IdCargaMasiva"													AS "IdCargaMasiva"
--			FROM :v_CargaMasivaDetalle AS CMD
--			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS TM 
--					ON TM."Nemotecnico" = CMD."Nemotecnico"
--			LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" VCR
--			ON VCR."IdEntidadClasificadoraRiesgo" = CMD."RV_IdEntidadClasificadoraRiesgo"
--			UNION ALL 
--			SELECT "Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasivaDetalle","IdCargaMasiva" 
--			FROM :v_TablaValidaciones;
			
		ELSE
		
		END IF;	
		
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
