SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEliminarCargaMasiva";
                                           
CREATE PROCEDURE
-- 
--  Proposito: Valida los requisitos para eliminar una carga masiva y su detalle
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 05/10/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosEliminarCargaMasiva"(
	IN  p_CargaMasivaEliminar       TABLE ("IdCargaMasiva"        BIGINT),	
	OUT p_out_RegistrosRech	        TABLE ("IdCargaMasiva"        BIGINT),  -- Rechazados por validación
	
	-- Salidas para mensajes de errores	
	OUT p_out_ErroresDetalle         TABLE ("Codigo" nvarchar(100), "Descripcion" nvarchar(520)),
	OUT p_out_AdvertenciasDetalle	 TABLE ("Codigo" nvarchar(100), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(520)),	
    OUT p_out_ResultadoRegla         NVARCHAR(40) --OK=Cumple todos los requisitos,		
											      --ADVE=Alguna de las reglas posee advertencias
                                                  --RECH=Alguna de las reglas detienen la operacion

) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_erroroutmsg  			NVARCHAR(520);
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT; 
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
		v_hASh = SELECT 'FolioCartera' AS "Codigo", 0 AS "Valor" FROM DUMMY;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hASh,:v_erroroutmsg);

		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
	END; 
	
	p_out_resultadoRegla	  := 'OK';
	p_out_ErroresDetalle	   = SELECT '' AS "Codigo",'' AS "Descripcion"  FROM dummy WHERE 1=2;
	p_out_AdvertenciasDetalle  = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_RegistrosRech	       = SELECT 0 AS "IdCargaMasiva" FROM dummy WHERE 1=2;

	
	
	
	
	BEGIN	
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio_ADVE	BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));

		DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(40), 
											   "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"IdCargaMasiva" BIGINT );
	
		
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdCargaMasiva"  FROM dummy WHERE (1=2);		


		------------------------------------------------------
		-- VALIDACIONES BASICAS
		------------------------------------------------------
 

		-------------------------------------------------
		-- VALIDACIONES DE NEGOCIO
		-------------------------------------------------

		-- Valida que la carga masiva exista y que no esté procesada
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",
				CASE WHEN COALESCE(CME."IdCargaMasiva",0) = 0 THEN 0
					 WHEN (CM."IdEstado" = 'PRO') THEN 0  -- Procesado
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(CME."IdCargaMasiva",0) = 0 THEN 'CARGA_MASIVA_NO_EXISTE'
					 WHEN (CM."IdEstado" = 'PRO') THEN 'CARGA_MASIVA_PROCESADA'
					 ELSE  ''
				END AS "CodigoError",				
				'IdCargaMasiva' AS "CodigoDato",
		       	CAST(CME."IdCargaMasiva" as NVARCHAR(50)) AS "ValorDato",
		       	CME."IdCargaMasiva"		       	
		FROM :p_CargaMasivaEliminar AS CME
		LEFT JOIN "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM ON (CM."IdCargaMasiva" = CME."IdCargaMasiva")
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasiva" FROM :v_TablaRequisitos;
		

	

		-------------------------------------------------
		-- ANALISIS DE DATOS
		-------------------------------------------------

		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" = 2 THEN 1 END)
	   	INTO v_error_General, v_error_Negocio_ADVE
	   	FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0      THEN 'RECH'
	   								 WHEN :v_error_Negocio_ADVE > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;

		-- Registros Rechazados por validación
	    p_out_RegistrosRech =
	    SELECT "IdCargaMasiva"
	    FROM :v_TablaRequisitos
	    WHERE "Prioridad" > 0
	    AND "Cumple" = 0;
	    
	    
	    v_TablaCodigosMensajes =
		SELECT 	"CodigoError" AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato" 
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0
		ORDER By "Prioridad" DESC;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,p_out_ErroresDetalle);

		 
		p_out_AdvertenciasDetalle =
		SELECT ED."Codigo",	
			   '' AS "RequiereAutorizacion",
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion" || ' (' || TR."IdCargaMasiva" || ')')
					ELSE ED."Descripcion"
			   END AS "Descripcion"
		 FROM :p_out_ErroresDetalle AS ED
		 INNER JOIN :v_TablaRequisitos AS TR ON (TR."CodigoError" = ED."Codigo")
		 WHERE TR."Cumple" = 0
		 AND   TR."Prioridad" > 1;  -- Errores de Negocio (ADVE)
		 
		
		p_out_ErroresDetalle =
		SELECT ED."Codigo",		
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion" || ' (' || TR."IdCargaMasiva" || ')')
					ELSE ED."Descripcion"
			   END AS "Descripcion"
		 FROM :p_out_ErroresDetalle AS ED
		 INNER JOIN :v_TablaRequisitos AS TR ON (TR."CodigoError" = ED."Codigo")
		 WHERE TR."Cumple" = 0
		 AND   TR."Prioridad" = 1;  -- Errores generales (RECH)

		 
    END;
	
END;
