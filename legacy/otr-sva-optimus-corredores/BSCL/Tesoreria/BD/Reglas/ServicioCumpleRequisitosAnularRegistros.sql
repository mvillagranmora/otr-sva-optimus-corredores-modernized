SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosAnularRegistros";

CREATE PROCEDURE
-- 
--  Proposito: Valida Movimientos y Comprobantes para su anulación
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 04/05/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosAnularRegistros"(
	-- Parametros entrada
    IN p_IdModulo 				NVARCHAR(3), 
	IN p_FechaMovimiento		NVARCHAR(10),
	IN p_EjecutaAutorizacion	NVARCHAR(1), -- S/N
	IN p_RegistrosAProcesar 	TABLE ("Id" BIGINT, "Entidad" NVARCHAR(300)),
	OUT p_out_RegistrosRech	    TABLE ("Id" BIGINT, "Entidad" NVARCHAR(300)),  -- Rechazados por validación
	OUT p_out_RegistrosRaut	    TABLE ("Id" BIGINT, "Entidad" NVARCHAR(300)),  -- Requiere autorizacion
	
	-- Salidas para mensajes de errores	
	OUT p_out_ErroresDetalle         TABLE ("Codigo" nvarchar(100), "Descripcion" nvarchar(520)),
	OUT p_out_AdvertenciasDetalle	 TABLE ("Codigo" nvarchar(100), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(520)),
	
    OUT p_out_ResultadoRegla     NVARCHAR(40) --OK=Cumple todos los requisitos,		
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
	p_out_RegistrosRech	       = SELECT 0 AS "Id", '' AS "Entidad" FROM dummy WHERE 1=2;
	p_out_RegistrosRaut		   = SELECT 0 AS "Id", '' AS "Entidad" FROM dummy WHERE 1=2;
	
	
	
	
	BEGIN	
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio_ADVE	BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));

		DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(80), 
											   "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"Id" BIGINT, "Entidad" NVARCHAR(300),"RequiereAutorizacion" NVARCHAR(1));
	
		
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "Id", '' AS "Entidad",'' AS "RequiereAutorizacion" FROM dummy WHERE (1=2);		

		------------------------------------------------------
		-- VALIDACIONES BASICAS (MOVIMIENTOS Y COMPROBANTES)
		------------------------------------------------------
		--Valida que la fecha se sistema sea válida
		v_TablaRequisitos =
		SELECT 	1 as "Prioridad",	
		       	CASE 
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaMovimiento) = 'N') OR (p_FechaMovimiento = '0001-01-01') THEN 0 
		       		ELSE 1 
				END AS "Cumple",				
		       	'FECHA_SISTEMA_INVALIDA' AS "CodigoError",
		       	'Fecha' AS "CodigoDato",
		       	:p_FechaMovimiento AS "ValorDato",
		       	0  AS "Id",
		       	'' AS "Entidad",
		       	'N' AS "RequiereAutorizacion"
		FROM DUMMY
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad","RequiereAutorizacion" FROM :v_TablaRequisitos;
				 
		-- Valida que venga Id de Movimiento o Comprobante
		v_TablaRequisitos =
		SELECT 	1 AS "Prioridad",	
		       	CASE 
		       		WHEN COUNT(RP."Id") < 1 THEN 0 
		       		ELSE 1 
				END AS "Cumple", 
		       	'NO_VIENEN_MOVIMIENTOS' AS "CodigoError",
		       	'' AS "CodigoDato",
		       	'' AS "ValorDato",
		       	0  AS "Id",
		       	'' AS "Entidad",
		       	'N' AS "RequiereAutorizacion"
		FROM :p_RegistrosAProcesar AS RP
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad","RequiereAutorizacion" FROM :v_TablaRequisitos;
		

		-- Valida que el Movimiento o Comprobante sea válido
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
				CASE WHEN (COALESCE(RP."Id",0) = 0 or RP."Id" = 0) THEN 0
					 ELSE 1
				END AS "Cumple",
				CASE WHEN RP."Entidad" = 'TES.IngresoEgreso' THEN 'MOVIMIENTO_TESORERIA_INVALIDO'
					 WHEN RP."Entidad" = 'TES.Comprobante'   THEN 'COMPROBANTE_TESORERIA_INVALIDO'  
				END AS "CodigoError",
				CASE WHEN RP."Entidad" = 'TES.IngresoEgreso' THEN 'IdIngresoEgreso'
					 WHEN RP."Entidad" = 'TES.Comprobante'   THEN 'IdComprobante'
				END AS "CodigoDato",
		       	CAST(RP."Id" as NVARCHAR(50)) AS "ValorDato",
		       	0 as "Id",
		       	RP."Entidad",
		       	'N' AS "RequiereAutorizacion"
		FROM :p_RegistrosAProcesar as RP
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad","RequiereAutorizacion" FROM :v_TablaRequisitos;
 

 
		-- Valida que la entidad ingresada sea válida
		v_TablaRequisitos = 
		SELECT  1 AS "Prioridad",
				CASE WHEN ((RP."Entidad" <> 'TES.IngresoEgreso') AND (RP."Entidad" <> 'TES.Comprobante')) THEN 0
					 ELSE 1
				END AS "Cumple",				
				CASE WHEN ((RP."Entidad" <> 'TES.IngresoEgreso') AND (RP."Entidad" <> 'TES.Comprobante')) THEN 'ENTIDAD_TESORERIA_INVALIDA'
				END AS "CodigoError",
				'Id' AS "CodigoDato",
		       	CAST(RP."Id" as NVARCHAR(50)) AS "ValorDato",
		       	RP."Id",
		       	RP."Entidad",
		       	'N' AS "RequiereAutorizacion"
		FROM :p_RegistrosAProcesar as RP
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad","RequiereAutorizacion" FROM :v_TablaRequisitos;

		
		-------------------------------------------------
		-- VALIDACIONES DE NEGOCIO (PARA MOVIMIENTOS)
		-------------------------------------------------

		-- Valida que Movimientos Existan y estado de estos
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",
				CASE WHEN COALESCE(IE."IdIngresoEgreso",0) = 0 THEN 0
					 WHEN (IE."IdEstado" = 'BLO') THEN 0
					 WHEN (IE."IdEstado" = 'ANU') THEN 0
					 WHEN (IE."IdEstado" = 'EMI' AND IE."IdModulo" != 'MRF') THEN 0
					 ELSE  1
				END AS "Cumple",
				
				CASE WHEN COALESCE(IE."IdIngresoEgreso",0) = 0 THEN 'MOVIMIENTO_TESORERIA_NO_EXISTE'
					 WHEN (IE."IdEstado" = 'BLO') THEN 'MOVIMIENTO_TESORERIA_BLOQUEADO'
					 WHEN (IE."IdEstado" = 'ANU') THEN 'MOVIMIENTO_TESORERIA_ANULADO'
					 WHEN (IE."IdEstado" = 'EMI') THEN 'MOVIMIENTO_TESORERIA_EMITIDO'
					 ELSE  ''
				END AS "CodigoError",				
				'IdIngresoEgreso' AS "CodigoDato",
		       	CAST(RP."Id" as NVARCHAR(50)) AS "ValorDato",
		       	RP."Id",
		       	RP."Entidad",
		       	'N' AS "RequiereAutorizacion"
		FROM :p_RegistrosAProcesar as RP
		LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = RP."Id")
		WHERE RP."Entidad" = 'TES.IngresoEgreso'
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad","RequiereAutorizacion" FROM :v_TablaRequisitos;
		


		-- Valida que la anulación de movimientos provenga del módulo que generó el movimiento
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",
				CASE WHEN (p_IdModulo <> COALESCE(IE."IdModulo",'') AND COALESCE(IE."IdModulo",'') = 'FAC') THEN 0
				     WHEN (p_IdModulo <> COALESCE(IE."IdModulo",'') AND COALESCE(IE."IdModulo",'') = 'CUS') THEN 0
				     WHEN (p_IdModulo <> COALESCE(IE."IdModulo",'') AND COALESCE(IE."IdModulo",'') = 'MRF') THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN (p_IdModulo <> COALESCE(IE."IdModulo",'') AND COALESCE(IE."IdModulo",'') = 'FAC') THEN 'MOVIMIENTO_TESORERIA_GENERADO_FACTURACION_NO_ANULAR'
				     WHEN (p_IdModulo <> COALESCE(IE."IdModulo",'') AND COALESCE(IE."IdModulo",'') = 'CUS') THEN 'MOVIMIENTO_TESORERIA_GENERADO_CUSTODIA_NO_ANULAR'
				     WHEN (p_IdModulo <> COALESCE(IE."IdModulo",'') AND COALESCE(IE."IdModulo",'') = 'MRF') THEN 'MOVIMIENTO_TESORERIA_GENERADO_MESA_RF_NO_ANULAR'
					 ELSE  ''
				END AS "CodigoError",
				'IdIngresoEgreso' AS "CodigoDato",
		       	CAST(RP."Id" AS NVARCHAR(50)) AS "ValorDato",
		       	RP."Id",
		       	RP."Entidad",
		       	'N' AS "RequiereAutorizacion"
		FROM :p_RegistrosAProcesar AS RP
		LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE ON (IE."IdIngresoEgreso" = RP."Id")
		WHERE RP."Entidad" = 'TES.IngresoEgreso'
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id", "Entidad","RequiereAutorizacion" FROM :v_TablaRequisitos;


		-------------------------------------------------
		-- VALIDACIONES DE NEGOCIO (PARA COMPROBANTES)
		-------------------------------------------------
		-- Valida que los comprobantes existan y el estado de estos
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",		
				CASE WHEN COALESCE(COM."IdComprobante",0) = 0 THEN 0
					 WHEN (COM."IdEstado" = 'BLO') THEN 0
					 WHEN (COM."IdEstado" = 'ANU') THEN 0
					 ELSE  1
				END AS "Cumple",
				
				CASE WHEN COALESCE(COM."IdComprobante",0) = 0 THEN 'COMPROBANTE_TESORERIA_NO_EXISTE'
					 WHEN (COM."IdEstado" = 'BLO') THEN 'COMPROBANTE_TESORERIA_BLOQUEADO'
					 WHEN (COM."IdEstado" = 'ANU') THEN 'COMPROBANTE_TESORERIA_ANULADO'
					 ELSE  ''
				END AS "CodigoError",
				
				'IdComprobante' AS "CodigoDato",				
		       	CAST(RP."Id" AS NVARCHAR(50)) AS "ValorDato",
		       	RP."Id",
				RP."Entidad",
				'N' AS "RequiereAutorizacion"
		FROM :p_RegistrosAProcesar AS RP		
		LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = RP."Id")
		WHERE RP."Entidad" = 'TES.Comprobante'
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad","RequiereAutorizacion" FROM :v_TablaRequisitos;



		-- Valida comprobantes que requieren de autorización para ser anulados
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",		
				CASE 
					 WHEN (COM."IdEstado" = 'LIQ' OR COM."IdEstado" = 'LIP') THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE 
					 WHEN (COM."IdEstado" = 'LIQ' OR COM."IdEstado" = 'LIP') THEN 'COMPROBANTE_LIQUIDADO_REQUIERE_AUTORIZACION'
					 ELSE  ''
				END AS "CodigoError",				
				'IdComprobante' AS "CodigoDato",				
		       	CAST(RP."Id" AS NVARCHAR(50)) AS "ValorDato",
		       	RP."Id",
				RP."Entidad",
				'S' AS "RequiereAutorizacion"
		FROM :p_RegistrosAProcesar AS RP		
		INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = RP."Id")				
		WHERE RP."Entidad" = 'TES.Comprobante'
		AND COM."IdEvento" < 1
		AND :p_EjecutaAutorizacion = 'S'  -- validar solo cuando se indica
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad","RequiereAutorizacion" FROM :v_TablaRequisitos;


		-- Valida estado de autorización de comprobantes(para liquidados y liquidados parcial)
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",
				CASE 
					 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'RZD') THEN 0
					 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'VCD') THEN 0
					 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'VIG') THEN 0					 
					 ELSE  1
				END AS "Cumple",
				CASE 
					 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'RZD') THEN 'EVENTO_SE_ENCUENTRA_RECHAZADO'
					 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'VCD') THEN 'EVENTO_SE_ENCUENTRA_VENCIDO'
					 WHEN (COM."IdEvento" > 0 AND EVE."IdEstado" = 'VIG') THEN 'EVENTO_SIN_AUTORIZACION'
					 ELSE  ''
				END AS "CodigoError",		       	
				'IdComprobante' AS "CodigoDato",				
		       	CAST(RP."Id" AS NVARCHAR(50)) AS "ValorDato",
		       	RP."Id",
				RP."Entidad",
				'N' AS "RequiereAutorizacion"
		FROM :p_RegistrosAProcesar AS RP		
		INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = RP."Id")		
		LEFT JOIN "BSCL.Plataforma.BD::AUT.Evento" AS EVE ON (EVE."IdEvento" = COM."IdEvento")
		WHERE RP."Entidad" = 'TES.Comprobante'		
		AND (COM."IdEstado" = 'LIQ' OR COM."IdEstado" = 'LIP')
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad","RequiereAutorizacion" FROM :v_TablaRequisitos;
		


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
	    SELECT "Id", "Entidad"
	    FROM :v_TablaRequisitos
	    WHERE "Prioridad" > 0
	    AND "RequiereAutorizacion" = 'N'
	    AND "Cumple" = 0;
	    
		-- Registros que requieren autorización
	    p_out_RegistrosRaut =
	    SELECT "Id", "Entidad"
	    FROM :v_TablaRequisitos
	    WHERE "Prioridad" = 2
	    AND "Cumple" = 0	    
	    AND "RequiereAutorizacion" = 'S';
		
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
			   TR."RequiereAutorizacion",
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion" || ' (' || TR."Id" || ')')
					ELSE ED."Descripcion"
			   END AS "Descripcion"
		 FROM :p_out_ErroresDetalle AS ED
		 INNER JOIN :v_TablaRequisitos AS TR ON (TR."CodigoError" = ED."Codigo")
		 WHERE TR."Cumple" = 0
		 AND   TR."Prioridad" > 1;  -- Errores de Negocio (ADVE)
		 
		
		p_out_ErroresDetalle =
		SELECT ED."Codigo",		
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion" || ' (' || TR."Id" || ')')
					ELSE ED."Descripcion"
			   END AS "Descripcion"
		 FROM :p_out_ErroresDetalle AS ED
		 INNER JOIN :v_TablaRequisitos AS TR ON (TR."CodigoError" = ED."Codigo")
		 WHERE TR."Cumple" = 0
		 AND   TR."Prioridad" = 1;  -- Errores generales (RECH)

		 
    END;
	
END;