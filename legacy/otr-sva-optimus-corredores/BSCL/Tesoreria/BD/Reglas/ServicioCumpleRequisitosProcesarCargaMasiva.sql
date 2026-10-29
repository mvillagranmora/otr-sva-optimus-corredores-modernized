SET SCHEMA "BSCL";
GO 
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosProcesarCargaMasiva";
GO
CREATE PROCEDURE
-- 
--  Proposito: Valida los registros de la carga masiva para ser procesados
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 07/10/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosProcesarCargaMasiva"(
	IN  p_IdEmpresa					BIGINT,
	IN  p_CargaMasivaProcesar       TABLE (
											  "IdCargaMasiva"        			BIGINT,
											  "CodigoTipoCargaMasiva"    		NVARCHAR(40),	     -- CARGA_RECAUDACION_SERVIPAG   /  CARGA_GENERICA_MOVIMIENTOS
											  "IdMoneda"			 			NVARCHAR(3),
											  "IdConcepto"           			NVARCHAR(6),
											  "IdMedioDePago"        			NVARCHAR(3),
										 	  "IdCuentaBanco"					BIGINT
										  ),

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
 
		-- Valida que exista preferencia de pago para la carga masiva(si es que no es carga genérica)
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
				CASE WHEN COALESCE(PPC."CodigoTipoCargaMasiva",'') = '' THEN 0				
	 				 WHEN (CMP."CodigoTipoCargaMasiva" = 'CARGA_RECAUDACION_SERVIPAG' AND COALESCE(PPC."IdPersonaEntidadRecaudadora",0) = 0) THEN 0
					 WHEN COALESCE(PPC."IdConcepto",'') = ''      THEN 0
					 WHEN COALESCE(PPC."IdCuentaBanco",0) = 0     THEN 0
					 WHEN COALESCE(PPC."IdMedioDePago",'') = ''   THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(PPC."CodigoTipoCargaMasiva",'') = '' THEN 'TIPO_CARGA_MASIVA_SIN_CONFIGURACION'
					 WHEN (CMP."CodigoTipoCargaMasiva" = 'CARGA_RECAUDACION_SERVIPAG' AND COALESCE(PPC."IdPersonaEntidadRecaudadora",0) = 0) THEN 'CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA'
					 WHEN COALESCE(PPC."IdConcepto",'') = ''      THEN 'CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA'
					 WHEN COALESCE(PPC."IdCuentaBanco",0) = 0     THEN 'CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA'
					 WHEN COALESCE(PPC."IdMedioDePago",'') = ''   THEN 'CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA'
					 ELSE  ''
				END AS "CodigoError",				
				'IdCargaMasiva' AS "CodigoDato",
		       	CAST(CMP."CodigoTipoCargaMasiva" as NVARCHAR(50)) AS "ValorDato",
		       	CMP."IdCargaMasiva"		       	
		FROM :p_CargaMasivaProcesar AS CMP
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" AS PPC ON 
		(	
			    PPC."CodigoTipoCargaMasiva" = CMP."CodigoTipoCargaMasiva"
			AND PPC."NombreEntidad" 	= 'TES.CargaMasiva'
			AND PPC."IdEstado" 			= 'ACT'
			AND PPC."IdEmpresa" 		= :p_IdEmpresa
						
		)
		WHERE CMP."CodigoTipoCargaMasiva" IN ('CARGA_RECAUDACION_SERVIPAG','CONCILIACION_RECAUDACION_BCP','EGRESOS_POR_ABONOS_BCP_PAC')
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasiva" FROM :v_TablaRequisitos;

	
		
		-- Valida que exista Tipo Carga Masiva
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
				CASE WHEN COALESCE(TCM."CodigoTipoCargaMasiva",'') = '' THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(TCM."CodigoTipoCargaMasiva",'') = '' THEN 'TIPO_CARGA_MASIVA_NO_EXISTE'
					 ELSE  ''
				END AS "CodigoError",				
				'IdCargaMasiva' AS "CodigoDato",
		       	CAST(TCM."CodigoTipoCargaMasiva" as NVARCHAR(50)) AS "ValorDato",
		       	CMP."IdCargaMasiva"		       	
		FROM :p_CargaMasivaProcesar AS CMP
		LEFT JOIN "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."CodigoTipoCargaMasiva" = CMP."CodigoTipoCargaMasiva")
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasiva" FROM :v_TablaRequisitos;

		
		-------------------------------------------------
		-- VALIDACIONES DE NEGOCIO
		-------------------------------------------------

		-- Valida que la carga masiva exista y que no esté procesada, ni invalida
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",
				CASE WHEN COALESCE(TCM."CodigoTipoCargaMasiva",'') = '' THEN 0
					 WHEN (CM."IdEstado" = 'PRO') THEN 0
					 WHEN (CM."IdEstado" = 'INV') THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(TCM."CodigoTipoCargaMasiva",'') = '' THEN 'CARGA_MASIVA_NO_EXISTE'
					 WHEN (CM."IdEstado" = 'PRO') THEN 'CARGA_MASIVA_PROCESADA'
					 WHEN (CM."IdEstado" = 'INV') THEN 'CARGA_MASIVA_INVALIDA'
					 ELSE  ''
				END AS "CodigoError",
				'IdCargaMasiva' AS "CodigoDato",
		       	CAST(CM."IdCargaMasiva" as NVARCHAR(50)) AS "ValorDato",
		       	CM."IdCargaMasiva"
		FROM :p_CargaMasivaProcesar AS CMP
		LEFT JOIN "BSCL.Tesoreria.BD::CAR.TipoCargaMasiva" AS TCM ON (TCM."CodigoTipoCargaMasiva" = CMP."CodigoTipoCargaMasiva")
		LEFT JOIN "BSCL.Tesoreria.BD::CAR.CargaMasiva" AS CM ON (CM."IdCargaMasiva" = CMP."IdCargaMasiva")
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdCargaMasiva" FROM :v_TablaRequisitos;
		


		-------------------------------------------------
		-- ANALISIS DE DATOS
		-------------------------------------------------
		-- SELECT 'TablaRequisitos', * FROM :v_TablaRequisitos; -- Revisar

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
