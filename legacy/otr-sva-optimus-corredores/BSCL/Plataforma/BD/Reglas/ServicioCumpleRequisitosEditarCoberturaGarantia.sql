SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosEditarCoberturaGarantia";

CREATE PROCEDURE
-- 
--  Proposito: Valida que los datos de cobertura garantía a editar sean válidos 
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 16/05/2016  
--     
"BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosEditarCoberturaGarantia"(
	-- Parametros entrada
    IN p_REF_ID                 	    BIGINT,	   -- 0: Cuando Crea   -- Id: Cuando Edita
	IN p_IdEntidadDestinoGarantia		BIGINT,	
    IN p_IdTipoInstrumentoExigeGarantia NVARCHAR(3),
    IN p_IdInstrumentoExigeGarantia  	BIGINT,
    IN p_IdTipoInstrumentoComoGarantia  NVARCHAR(3),    
    IN p_IdEntidadClasificadoraRiesgo   NVARCHAR(10),
    IN p_ClasificacionRiesgo       		NVARCHAR(20),

	-- Salidas para mensajes de errores
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)),
	OUT p_out_resultadoRegla            NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
												        --ADVE= 	 								  	   
                                                        --RECH=Alguna de lAS reglAS detienen la operacion
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_erroroutmsg  			NVARCHAR(5000);
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		p_out_resultadoRegla := 'RECH';	
		v_hash = SELECT '' AS "Codigo", '' AS "Valor" FROM DUMMY;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		p_out_resultadoRegla 	:= 'RECH';
		v_ErrorNoData 			:= 'DATA_NO_EXISTE';
		v_hash					 = SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
	END; 
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_MovimientosRech	= SELECT 0  AS "IdComprobante" FROM dummy WHERE 1=2;
	
	BEGIN
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE  ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(5000));
		DECLARE v_TablaRequisitos		TABLE  ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100), 
												"CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(5000));	
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN						
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		--Inicializacion de variables
		v_TablaRequisitos = SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato" FROM dummy WHERE (1=2);
		


		------------------------------------------------------------------
		-- VALIDACIONES DE NEGOCIO
		------------------------------------------------------------------
		-- Valida que la entidad destino garantía exista
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
		       	CASE WHEN COUNT("IdEntidadDestinoGarantia") < 1 THEN 0 
		       		 ELSE 1 
				END AS "Cumple", 
		       	CASE WHEN COUNT("IdEntidadDestinoGarantia") < 1 THEN 'ENTIDAD_DESTINO_GARANTIA_NO_EXISTE'
		       		 ELSE ''
				END AS "CodigoError", 		
				'IdEntidadDestinoGarantia' AS "CodigoDato",
		       	CAST(:p_IdEntidadDestinoGarantia as NVARCHAR(50)) AS "ValorDato"
		FROM "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" AS ENT
		WHERE ENT."IdEntidadDestinoGarantia" = :p_IdEntidadDestinoGarantia		
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;


		-- Valida que el tipo instrumento(ExigeGarantia) exista
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
		       	CASE WHEN COUNT("IdTipoInstrumento") < 1 THEN 0 
		       		 ELSE 1 
				END AS "Cumple", 
		       	CASE WHEN COUNT("IdTipoInstrumento") < 1 THEN 'TIPO_INSTRUMENTO_NO_EXISTE'
		       		 ELSE ''
				END AS "CodigoError", 		
				'IdTipoInstrumentoExigeGarantia' AS "CodigoDato",
		       	CAST(:p_IdTipoInstrumentoExigeGarantia as NVARCHAR(50)) AS "ValorDato"
		FROM "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI
		WHERE TI."IdTipoInstrumento" = :p_IdTipoInstrumentoExigeGarantia
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;

    
		-- Valida que el instrumento(ExigeGarantia) exista
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
		       	CASE WHEN COUNT("IdInstrumento") < 1 THEN 0 
		       		 ELSE 1 
				END AS "Cumple", 
		       	CASE WHEN COUNT("IdInstrumento") < 1 THEN 'INSTRUMENTO_NO_EXISTE'
		       		 ELSE ''
				END AS "CodigoError", 		
				'IdInstrumentoExigeGarantia' AS "CodigoDato",
		       	CAST(:p_IdInstrumentoExigeGarantia as NVARCHAR(50)) AS "ValorDato"
		FROM "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		WHERE INS."IdInstrumento" = :p_IdInstrumentoExigeGarantia
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;
		

		-- Valida que el tipo instrumento(ComoGarantia) exista
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
		       	CASE WHEN COUNT("IdTipoInstrumento") < 1 THEN 0 
		       		 ELSE 1 
				END AS "Cumple", 
		       	CASE WHEN COUNT("IdTipoInstrumento") < 1 THEN 'INSTRUMENTO_NO_EXISTE'
		       		 ELSE ''
				END AS "CodigoError", 		
				'IdTipoInstrumentoComoGarantia' AS "CodigoDato",
		       	CAST(:p_IdTipoInstrumentoComoGarantia as NVARCHAR(50)) AS "ValorDato"
		FROM "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TI
		WHERE TI."IdTipoInstrumento" = :p_IdTipoInstrumentoComoGarantia
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;


 
		-- Valida que la cobertura a crear no exista
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
		       	CASE WHEN COUNT("IdCoberturaGarantia") > 0 THEN 0
		       		 ELSE 1
				END AS "Cumple",
		       	CASE WHEN COUNT("IdCoberturaGarantia") > 0 THEN 'COBERTURA_EXISTE'
		       		 ELSE ''
				END AS "CodigoError",
				'IdCoberturaGarantia' AS "CodigoDato",
		       	CAST(MAX(COB."IdCoberturaGarantia") as NVARCHAR(50)) AS "ValorDato"
		FROM "BSCL.Plataforma.BD::GTA.CoberturaGarantia" AS COB
		WHERE "IdEntidadDestinoGarantia" 		= :p_IdEntidadDestinoGarantia
		AND	  "IdTipoInstrumentoExigeGarantia" 	= :p_IdTipoInstrumentoExigeGarantia
		AND	  "IdInstrumentoExigeGarantia" 		= :p_IdInstrumentoExigeGarantia
		AND	  "IdTipoInstrumentoComoGarantia" 	= :p_IdTipoInstrumentoComoGarantia		
		and   "IdEntidadClasificadoraRiesgo" 	= :p_IdEntidadClasificadoraRiesgo
		AND	  "ClasificacionRiesgo" 			= :p_ClasificacionRiesgo
		AND   "IdCoberturaGarantia"  			<> :p_REF_ID
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;

				
		------------------------------------------------------------------
		-- ANALISIS DE LOS DATOS
		------------------------------------------------------------------
	   	 SELECT  COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   			INTO v_error_General, v_error_Negocio
	   	  FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
	   								 WHEN :v_error_Negocio > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;
	       
	    v_TablaCodigosMensajes =
		SELECT 	"CodigoError" AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato" 
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0
		ORDER By "Prioridad" DESC;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,p_out_ErroresDetalle);
		
		
	    p_out_ErroresDetalle =
		SELECT ED."Codigo",
			   ED."Descripcion"			   
		FROM :p_out_ErroresDetalle as ED
		INNER JOIN :v_TablaRequisitos as TR ON TR."CodigoError" = ED."Codigo"
		WHERE "Cumple" = 0;	
	    
    END;	
END;
