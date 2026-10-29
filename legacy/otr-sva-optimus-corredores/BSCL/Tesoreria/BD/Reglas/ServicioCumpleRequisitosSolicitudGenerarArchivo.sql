SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosSolicitudGenerarArchivo";

CREATE PROCEDURE
-- 
--  Proposito: Validar si la solicitud de generación de archivo de transferencias bancarias cumple los requisitos.
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 03/08/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosSolicitudGenerarArchivo"(
	IN p_IdEmpresa 					    BIGINT,  
	IN p_ArchivosAGenerar 				TABLE(
											"IdNominaTransferencia" BIGINT,
											"IdBanco"  				BIGINT,
											"IdMoneda" 				NVARCHAR(3),
											"IdMedioDePago" 		NVARCHAR(3),
										 	"IdTipoTarea"			NVARCHAR(40),
											"IdAgenda"	 		    BIGINT,
											"Segundos"				BIGINT
									 	),


	OUT p_out_RechazadosPorValidacion   TABLE("IdNominaTransferencia" BIGINT),
	
	-- Salidas para mensajes de errores
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)),
	OUT p_out_resultadoRegla            NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
												        --ADVE = 	 								  	   
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
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT; 
	

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
		DECLARE v_TablaCodigosMensajes	TABLE  ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));		
		DECLARE v_TablaRequisitos		TABLE  ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100), 
												"CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"IdNominaTransferencia" BIGINT);

		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN						
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		--Inicializacion de variables
		v_TablaRequisitos = SELECT  0 AS "Prioridad",0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato",'' AS "ValorDato",
								    0 as "IdNominaTransferencia" 
							FROM dummy WHERE (1=2);
		
		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;
				

		----------------------------------------------------------
		-- VALIDACIONES BASICAS
		----------------------------------------------------------

		-- Valida que vengan solicitudes
		v_TablaRequisitos =
		SELECT 	 1 AS "Prioridad"
		       	,CASE 
		       		WHEN COUNT("IdNominaTransferencia") < 1 THEN 0 
		       		ELSE 1 
				 END AS "Cumple" 
		       	,'NO_VIENEN_MOVIMIENTOS' AS "CodigoError"
		       	,'' AS "CodigoDato"
		       	,'' AS "ValorDato"
		       	,0 as "IdNominaTransferencia"
		FROM :p_ArchivosAGenerar
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdNominaTransferencia" FROM :v_TablaRequisitos;
		
		
		-- Valida que solicitudes sean reales
		v_TablaRequisitos = 
		SELECT   1 AS "Prioridad"
				,CASE WHEN (COALESCE("IdNominaTransferencia",0) = 0 or "IdNominaTransferencia" = 0) THEN 0  
					 ELSE 1
				 END AS "Cumple"
				,'TRANSFERENCIA_TESORERIA_NO_EXISTE' AS "CodigoError"
				,'IdNominaTransferencia' AS "CodigoDato"				
		       	,CAST("IdNominaTransferencia" as NVARCHAR(50)) AS "ValorDato"
		       	,0 as "IdNominaTransferencia"
		FROM :p_ArchivosAGenerar
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdNominaTransferencia" FROM :v_TablaRequisitos;
	

		
		
		-- Valida que exista configuracion para el formato del archivo		
		v_TablaRequisitos = 
		SELECT   1 AS "Prioridad"
				,CASE WHEN COALESCE(FA."IdFormatoArchivo",0) = 0 THEN 0
				 	 WHEN (FA."Servicio" = '') THEN 0
					 WHEN (FA."FormatoSalida" = '') THEN 0
					 ELSE  1
				 END AS "Cumple"				 
				,CASE WHEN COALESCE(FA."IdFormatoArchivo",0) = 0 THEN 'CONFIGURACION_FORMATO_ARCHIVO_NO_EXISTE'
				 	 WHEN (FA."Servicio" = '') THEN 'CONFIGURACION_FORMATO_ARCHIVO_SIN_SERVICIO'
					 WHEN (FA."FormatoSalida" = '') THEN 'CONFIGURACION_FORMATO_ARCHIVO_SIN_FORMATO_SALIDA'
					 ELSE  ''                             
				 END AS "CodigoError"				 
				,'IdNominaTransferencia' AS "CodigoDato"
		       	,CAST(AG."IdNominaTransferencia" as NVARCHAR(50)) AS "ValorDato"
		       	,AG."IdNominaTransferencia" AS "IdNominaTransferencia"
		FROM :p_ArchivosAGenerar AS AG
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarFormatoArchivo" AS FA ON (AG."IdBanco" = FA."IdBanco"
																			AND AG."IdMoneda" = FA."IdMoneda"
																			AND AG."IdMedioDePago" = FA."IdMedioDePago"
																			AND FA."IdEmpresa" = :p_IdEmpresa
																			AND FA."IdEstado" = 'ACT')		
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdNominaTransferencia" FROM :v_TablaRequisitos;



		----------------------------------------------------------
		-- NEGOCIO
		----------------------------------------------------------

		-- Valida que la transferencia exista y que no esté anulada ni recepcionada
		v_TablaRequisitos = 
		SELECT   2 AS "Prioridad"
				,CASE WHEN COALESCE(NT."IdNominaTransferencia",0) = 0 THEN 0
				 	 WHEN (NT."IdEstado" = 'ANU') THEN 0
					 WHEN (NT."IdEstado" = 'ING') THEN 0
					 ELSE  1
				 END AS "Cumple"				 
				,CASE WHEN COALESCE(NT."IdNominaTransferencia",0) = 0 THEN 'TRANSFERENCIA_TESORERIA_NO_EXISTE'
				 	 WHEN (NT."IdEstado" = 'ANU') THEN 'TRANSFERENCIA_TESORERIA_ANULADA'
					 WHEN (NT."IdEstado" = 'ING') THEN 'TRANSFERENCIA_TESORERIA_INGRESADA'
					 ELSE  ''
				 END AS "CodigoError"
				,'IdNominaTransferencia' AS "CodigoDato"
		       	,CAST(AG."IdNominaTransferencia" as NVARCHAR(50)) AS "ValorDato"
		       	,AG."IdNominaTransferencia" AS "IdNominaTransferencia"
		FROM :p_ArchivosAGenerar AS AG
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" AS NT  ON (NT."IdNominaTransferencia" = AG."IdNominaTransferencia" AND NT."IdEmpresa" = :p_IdEmpresa)
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdNominaTransferencia" FROM :v_TablaRequisitos;


		-- SELECT 'v_TablaRequisitos', * FROM :v_TablaRequisitos; -- BORRAR


		----------------------------------------------------------
		-- ANALISIS DE DATOS
		----------------------------------------------------------
	   	SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   			INTO v_error_General, v_error_Negocio
	   	FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
	   								 WHEN :v_error_Negocio > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;
	    
	    
	    p_out_RechazadosPorValidacion =
	    SELECT DISTINCT "IdNominaTransferencia"
	    FROM :v_TablaRequisitos
	    WHERE "Prioridad" > 1
	    AND "Cumple" = 0;	   	
	
	       
	    v_TablaCodigosMensajes =
		SELECT
			    "CodigoError"  AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato"
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0
		ORDER By "Prioridad" DESC;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalle);
		

	    p_out_ErroresDetalle =
		SELECT ED."Codigo",
			   ED."Descripcion"
		FROM :v_out_ErroresDetalle as ED;	
	
		 
    END;	
END;
