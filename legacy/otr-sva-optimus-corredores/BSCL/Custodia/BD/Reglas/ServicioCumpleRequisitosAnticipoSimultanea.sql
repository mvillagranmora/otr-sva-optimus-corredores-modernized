SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Custodia.BD.Reglas::ServicioCumpleRequisitosAnticipoSimultanea";
GO
CREATE PROCEDURE
-- 
--  Proposito: Validar 
--  Autor: NRM - BCS
--  Fecha Creacion: 23/11/2015  
--     
"BSCL.Custodia.BD.Reglas::ServicioCumpleRequisitosAnticipoSimultanea"(
	-- Parametros entrada
	IN 	p_FechaSistema				NVARCHAR(40),
	IN  p_FolioCartera				BIGINT,
	
	IN  p_IdCondicionLiquidacion	NVARCHAR(2), 
    IN  p_FechaCondicionLiquidacion	NVARCHAR(10),
	IN  p_CantidadAnticipo			DECIMAL(26,8),
	IN  p_TasaAnticipo				DECIMAL(26,8),
	IN  p_MontoAnticipo				DECIMAL(26,8),

	-- Salidas para mensajes de errores
--p_out_ErroresDetalle=BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosAnticipoSimultanea_Salida_ErroresDetalle
	OUT p_out_resultadoRegla        NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
													--ADVE = 	 								  	   
                                                   	--RECH=Alguna de las reglas detienen la operacion
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT; 
	
	DECLARE v_TablaCodigosMensajes TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),
											"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520))
					DEFAULT SELECT 0 AS "IdInterno",'' AS "Codigo", '' AS "CodigoDato1", '' AS "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2" FROM DUMMY;
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
		v_TablaCodigosMensajes = SELECT 1 AS "IdInterno", 'DATA_NO_EXISTE' AS "Codigo", 'FolioCartera' as "CodigoDato1", p_FolioCartera as "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2" FROM DUMMY;		
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_erroroutmsg);
		
		INSERT INTO "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosAnticipoSimultanea_Salida_ErroresDetalle" ("Codigo","Descripcion")
		SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
	END; 
	
	p_out_resultadoRegla	:= 'OK';

	
	
	BEGIN	
		DECLARE v_QuantityError            		BIGINT;
		DECLARE v_EsFecha						NVARCHAR(40);
		DECLARE v_TablaRequisitos     			TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(40), 
														"CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520));			
		
		--VARIABLES DE LA OPERACION
		DECLARE v_FolioCartera					BIGINT;
		DECLARE v_FechaVencimientoOper			NVARCHAR(40);
		DECLARE v_FechaLiquidacionOper			NVARCHAR(10);		
		DECLARE v_FechaAnticipoPermitida		NVARCHAR(40);
		DECLARE v_INGCantidad					DECIMAL(26,8);
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_TablaCodigosMensajes = SELECT 1 AS "IdInterno", 'DATA_NO_EXISTE' AS "Codigo", 'FolioCartera' as "CodigoDato1", p_FolioCartera as "ValorDato1", '' AS "CodigoDato2", '' AS "ValorDato2" FROM DUMMY;		
			
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_erroroutmsg);
			
			INSERT INTO "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosAnticipoSimultanea_Salida_ErroresDetalle" ("Codigo","Descripcion")
			SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		END;

		
		v_TablaRequisitos    = SELECT  0 as "Prioridad", 0 as "Cumple",'' as "CodigoError", 
									  '' as "CodigoDato", '' as "ValorDato" FROM dummy WHERE (1=2);
		

		--********************** VALIDACIONES BASICAS ***********************************
		v_TablaRequisitos =
		SELECT 	1 as "Prioridad",	
		       	CASE 
		       		WHEN p_CantidadAnticipo < 1 THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'CANTIDAD_MENOR_IGUAL_A_CERO' as "CodigoError", 
		       	'' as "CodigoDato",
		       	'' as "ValorDato"  
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;

		v_TablaRequisitos =
		SELECT 	1 as "Prioridad",	
		       	CASE 
		       		WHEN p_MontoAnticipo < 1 THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'MONTO_MENOR_IGUAL_A_CERO' as "CodigoError", 
		       	'' as "CodigoDato",
		       	'' as "ValorDato"  
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;


		v_TablaRequisitos =
		SELECT 	1 as "Prioridad",	
		       	CASE 
		       		WHEN LENGTH(COALESCE(p_IdCondicionLiquidacion,'')) = 0 THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'CONDICION_LIQUIDACION_INVALIDO' as "CodigoError", 
		       	'' as "CodigoDato",
		       	'' as "ValorDato"  
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;
		
		
		v_TablaRequisitos =
		SELECT 	1 as "Prioridad",	
		       	CASE 
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaCondicionLiquidacion) = 'N') OR (p_FechaCondicionLiquidacion = '0001-01-01') THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'FECHA_LIQUIDACION_INVALIDA' as "CodigoError", 
		       	'' as "CodigoDato",
		       	'' as "ValorDato"  
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;


		--********************** VALIDACIONES OPERACIONALES ***********************************
		--) OBTENER INFORMACION DE LA SIMULTANEA	
		SELECT
			"FechaVencimientoOperacion","FechaLiquidacion","INGCantidad"
		INTO
			v_FechaVencimientoOper,v_FechaLiquidacionOper,v_INGCantidad	
		FROM "BSCL.Custodia.BD::CUS.Cartera"
		WHERE "FolioCartera" = :p_FolioCartera
		AND "IdEstado" = 'VIG'; 


		SELECT ADD_DAYS (TO_DATE (v_FechaLiquidacionOper, 'YYYY-MM-DD'), 1) "add days" INTO v_FechaAnticipoPermitida  FROM DUMMY;

		v_TablaRequisitos =
		SELECT 	2 as "Prioridad",	
		       	CASE 
		       		WHEN TO_DATE(v_FechaVencimientoOper,'YYYY-MM-DD') < TO_DATE(p_FechaSistema,'YYYY-MM-DD') THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'FECHA_SISTEMA_MAYOR_VCTO_OPERACION' as "CodigoError", 
		       	'FechaVencOperacion' as "CodigoDato",
		       	v_FechaVencimientoOper as "ValorDato"  
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;
		

		v_TablaRequisitos =
		SELECT 	2 as "Prioridad",	
		       	CASE 
		       		WHEN TO_DATE(v_FechaLiquidacionOper,'YYYY-MM-DD') > TO_DATE(p_FechaSistema,'YYYY-MM-DD') THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'FECHA_SISTEMA_MENOR_LIQUIDACION' as "CodigoError", 
		       	'FechaLiquidacion' as "CodigoDato",
		       	v_FechaLiquidacionOper as "ValorDato" 
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;
			
		
		v_TablaRequisitos =
		SELECT 	2 as "Prioridad",	
		       	CASE 
		       		WHEN TO_DATE(v_FechaAnticipoPermitida,'YYYY-MM-DD') > TO_DATE(p_FechaSistema,'YYYY-MM-DD') THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'FECHA_SISTEMA_MENOR_ANTICIPO' as "CodigoError", 
		       	'FechaAnticipoPermitida' as "CodigoDato",
		       	v_FechaAnticipoPermitida as "ValorDato"  
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;
				
		v_TablaRequisitos =
		SELECT 	2 as "Prioridad",	
		       	CASE 
		       		WHEN TO_DATE(p_FechaCondicionLiquidacion,'YYYY-MM-DD') < TO_DATE(p_FechaSistema,'YYYY-MM-DD') THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'FECHA_LIQUIDACION_INVALIDA' as "CodigoError", 
		       	'Fecha' as "CodigoDato",
		       	p_FechaCondicionLiquidacion as "ValorDato"  
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;

		v_TablaRequisitos =
		SELECT 	2 as "Prioridad",	
		       	CASE 
		       		WHEN TO_DATE(v_FechaVencimientoOper,'YYYY-MM-DD') < TO_DATE(p_FechaCondicionLiquidacion,'YYYY-MM-DD') THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'FECHA_VCTO_MENOR_FECHA_LIQUIDACION' as "CodigoError", 
		       	'FechaLiquidacion' as "CodigoDato",
		       	p_FechaCondicionLiquidacion as "ValorDato"  
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;


		v_TablaRequisitos =
		SELECT 	2 as "Prioridad",	
		       	CASE 
		       		WHEN (p_CantidadAnticipo > v_INGCantidad) THEN 0 
		       		ELSE 1 
				END as "Cumple", 
		       	'CANTIDAD_ANTICIPAR' as "CodigoError", 
		       	'' as "CodigoDato",
		       	'' as "ValorDato"  
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato" FROM :v_TablaRequisitos;
		


		--********************** ANALISIS DE DATOS ***********************************
	   	SELECT COUNT(1) INTO v_QuantityError FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	     
		IF v_QuantityError > 0 THEN
			p_out_resultadoRegla := 'RECH';
			
			v_TablaCodigosMensajes =
					SELECT 1 AS "IdInterno"
						,"CodigoError" AS "Codigo"
						,"CodigoDato" AS "CodigoDato1"
						,"ValorDato" AS "ValorDato1"
						,'' AS "CodigoDato2"
						,'' AS "ValorDato2"
					  FROM :v_TablaRequisitos
					 WHERE "Cumple" = 0
					 ORDER By "Prioridad" DESC;

			  
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_ErroresDetalle);
			
			INSERT INTO "BSCL.Custodia.BD::GBL.ServicioCumpleRequisitosAnticipoSimultanea_Salida_ErroresDetalle" ("Codigo","Descripcion")
			SELECT a."Codigo", a."Descripcion" FROM :v_out_ErroresDetalle a;
			
		END IF;
    END;
END;
GO