SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosAnularNominaTransferencia";

CREATE PROCEDURE
-- 
--  Proposito: Validar si se cumplen con los requisitos solicitados
--  Autor: NRM - BCS
--  Fecha Creacion: 10/03/2016
--     
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosAnularNominaTransferencia"(
	-- Parametros entrada
	IN p_FechaMovimiento		NVARCHAR(10),
    IN p_RegistrosAProcesar 	TABLE ("Id" BIGINT, "Entidad" NVARCHAR(300)),
 

	OUT p_out_MovimientosRech	TABLE ("Id" BIGINT, "Entidad" NVARCHAR(300)),
	
	-- SalidAS para mensajes de errores
	OUT p_out_ErroresDetalle    TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(520)),
	OUT p_out_resultadoRegla    NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
												--ADVE = 	 								  	   
                                                --RECH=Alguna de lAS reglAS detienen la operacion
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
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
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_MovimientosRech	= SELECT 0 AS "Id",'' AS "Entidad" FROM dummy WHERE 1=2;
	
	BEGIN	
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(5000));
		DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(40),"CodigoDato" NVARCHAR(100), 
											   "ValorDato" NVARCHAR(520),"Id" BIGINT,"Entidad" NVARCHAR(300));			
		
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato",
									     '' AS "ValorDato",0 AS "Id",'' AS "Entidad" FROM dummy WHERE (1=2);
		

		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************		
		v_TablaRequisitos =
		SELECT 	1 as "Prioridad",	
		       	CASE 
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaMovimiento) = 'N') OR (p_FechaMovimiento = '0001-01-01') THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'FECHA_SISTEMA_INVALIDA' as "CodigoError", 
		       	'Fecha' as "CodigoDato",
		       	:p_FechaMovimiento as "ValorDato",
		       	0 as "Id",
		       	'' as "Entidad"
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad" FROM :v_TablaRequisitos;
		 
		v_TablaRequisitos =
		SELECT 	1 AS "Prioridad",	
		       	CASE 
		       		WHEN COUNT("Id") < 1 THEN 0 
		       		ELSE 1 
				END AS "Cumple", 
		       	'NO_VIENEN_MOVIMIENTOS' AS "CodigoError", 
		       	'' AS "CodigoDato",
		       	'' AS "ValorDato",
		       	0 as "Id",
		       	'' as "Entidad"
		  FROM :p_RegistrosAProcesar
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad" FROM :v_TablaRequisitos;

		--*********************************************************************************************
		--***************************************  NEGOCIO  *******************************************
		--*********************************************************************************************	
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",
				CASE WHEN COALESCE(NT."IdNominaTransferencia",0) = 0 THEN 0
				 	 WHEN (NT."IdEstado" = 'RCP') THEN 0
					 WHEN (NT."IdEstado" = 'ANU') THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(NT."IdNominaTransferencia",0) = 0 THEN 'NOMINA_TRANSFERENCIA_NO_EXISTE'
				 	 WHEN (NT."IdEstado" = 'RCP') THEN 'NOMINA_TRANSFERENCIA_RECEPCIONADO'
					 WHEN (NT."IdEstado" = 'ANU') THEN 'NOMINA_TRANSFERENCIA_ANULADO'
					 ELSE  ''
				END AS "CodigoError",
				'IdNominaTransferencia' AS "CodigoDato",
		       	CAST(RP."Id" as NVARCHAR(50)) AS "ValorDato",
		       	0 as "Id",
		       	'' as "Entidad"
		 FROM :p_RegistrosAProcesar as RP
		 LEFT JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = RP."Id"
		WHERE RP."Entidad" = 'NominaTransferencia'
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad" FROM :v_TablaRequisitos;
		
		v_TablaRequisitos = 
		SELECT  2 AS "Prioridad",
				CASE WHEN COALESCE(NT."IdNominaTransferenciaDetalle",0) = 0 THEN 0
				 	 WHEN (NT."IdEstado" = 'RCP') THEN 0
					 WHEN (NT."IdEstado" = 'ANU') THEN 0
					 ELSE  1
				END AS "Cumple",
				CASE WHEN COALESCE(NT."IdNominaTransferenciaDetalle",0) = 0 THEN 'NOMINA_TRANSFERENCIA_DETALLE_NO_EXISTE'
				 	 WHEN (NT."IdEstado" = 'RCP') THEN 'NOMINA_TRANSFERENCIA_DETALLE_RECEPCIONADO'
					 WHEN (NT."IdEstado" = 'ANU') THEN 'NOMINA_TRANSFERENCIA_DETALLE_ANULADO'
					 ELSE  ''
				END AS "CodigoError",
				'IdNominaTransferenciaDetalle' AS "CodigoDato",
		       	CAST(RP."Id" as NVARCHAR(50)) AS "ValorDato",
		       	0 as "Id",
		       	'' as "Entidad"
		 FROM :p_RegistrosAProcesar as RP
		 LEFT JOIN "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NT ON NT."IdNominaTransferenciaDetalle" = RP."Id"
		WHERE RP."Entidad" = 'NominaTransferenciaDetalle'
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","Id","Entidad" FROM :v_TablaRequisitos;

		--*********************************************************************************************
		--********************************* ANALISIS DE DATOS *****************************************
		--*********************************************************************************************

		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
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
		 
		 
		p_out_MovimientosRech =
	    SELECT "Id", "Entidad"
	      FROM :v_TablaRequisitos
	     WHERE "Prioridad" > 1
	       AND "Cumple" = 0; 
		
		p_out_ErroresDetalle =
		SELECT ED."Codigo",
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion" || ' (' || TR."Id" || ')')
					ELSE ED."Descripcion"
				END as "Descripcion"
		  FROM :p_out_ErroresDetalle as ED
		 INNER JOIN :v_TablaRequisitos as TR ON TR."CodigoError" = ED."Codigo"
		 WHERE "Cumple" = 0;
    END;	
END;