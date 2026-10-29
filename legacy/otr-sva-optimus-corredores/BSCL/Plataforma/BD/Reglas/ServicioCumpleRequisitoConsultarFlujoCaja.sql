SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitoConsultarFlujoCaja";

CREATE PROCEDURE
-- 
--  Proposito: validaciones para consultar el flujo de caja
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 18/06/2016
--     
"BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitoConsultarFlujoCaja" (
	-- Parametros entrada
	IN  p_IdEmpresa					BIGINT,
	IN  p_Uso                      	NVARCHAR(40),          -- Uso del flujo de caja	
	IN  p_FechaDesde				NVARCHAR(10),
    IN  p_FechaHasta				NVARCHAR(10),
    
    IN  p_PortafolioAConsultar		TABLE("IdInterno"		BIGINT,
    									  "IdPortafolio"	BIGINT,		  
    									  "IdMoneda"		NVARCHAR(3)),
    
	OUT p_out_MovimientosRech		TABLE ("IdInterno"		BIGINT),
	OUT p_out_ErroresDetalle		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),
	OUT p_out_resultadoRegla		NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
													--ADVE = 	 								  	   
                                                	--RECH=Alguna de lAS reglAS detienen la operacion
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION;	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo","Descripcion" FROM :v_erroroutmsg;
	END;
	
	p_out_resultadoRegla	:= 'OK';
	p_out_ErroresDetalle	= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_MovimientosRech	= SELECT  0 AS "IdInterno" FROM dummy WHERE 1=2;
	
	
	BEGIN
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		v_TablaCodigosMensajes	= SELECT  0 AS "IdInterno",'' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",
										 '' AS "CodigoDato2",'' AS "ValorDato2" FROM dummy WHERE 1=2;
		v_TablaRequisitos 		= SELECT  0 AS "Prioridad",0 AS "Cumple",'' AS "Codigo",'' AS "CodigoDato1",'' AS "ValorDato1",
								   		 '' AS "CodigoDato2",'' AS "ValorDato2",0 AS "IdInterno" FROM dummy WHERE (1=2);



		--**********************************************************************************************
		--********************************* VALIDACIONES BASICAS ***************************************
		--**********************************************************************************************
		v_TablaRequisitos =
		SELECT	 1 AS "Prioridad"
		       	,CASE
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaDesde) = 'N') OR (p_FechaDesde = '0001-01-01') THEN 0
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaHasta) = 'N') OR (p_FechaHasta = '0001-01-01') THEN 0
		       		WHEN TO_DATE(p_FechaHasta,'YYYY-MM-DD') < TO_DATE(p_FechaDesde,'YYYY-MM-DD') THEN 0
		       		ELSE 1
				 END AS "Cumple"
				,CASE
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaDesde) = 'N') OR (p_FechaDesde = '0001-01-01') THEN 'FECHA_NO_VALIDA'
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaHasta) = 'N') OR (p_FechaHasta = '0001-01-01') THEN 'FECHA_NO_VALIDA'
		       		WHEN TO_DATE(p_FechaHasta,'YYYY-MM-DD') < TO_DATE(p_FechaDesde,'YYYY-MM-DD') THEN 'RANGO_FECHA_INVALIDO'
		       		ELSE ''
				 END AS "Codigo"
				,CASE
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaDesde) = 'N') OR (p_FechaDesde = '0001-01-01') THEN 'Fecha Desde'
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaHasta) = 'N') OR (p_FechaHasta = '0001-01-01') THEN 'Fecha Hasta'
		       		WHEN TO_DATE(p_FechaHasta,'YYYY-MM-DD') < TO_DATE(p_FechaDesde,'YYYY-MM-DD') THEN ''
		       		ELSE ''
				 END AS "CodigoDato1"
				,CASE
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaDesde) = 'N') OR (p_FechaDesde = '0001-01-01') THEN p_FechaDesde
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaHasta) = 'N') OR (p_FechaHasta = '0001-01-01') THEN p_FechaHasta
		       		WHEN TO_DATE(p_FechaHasta,'YYYY-MM-DD') < TO_DATE(p_FechaDesde,'YYYY-MM-DD') THEN ''
		       		ELSE ''
				 END AS "ValorDato1"
				 ,'' as "CodigoDato2"
				 ,'' as "ValorDato2"				 
				 ,0  as "IdInterno"
		FROM DUMMY
		UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","IdInterno" FROM :v_TablaRequisitos;
		
		
		v_TablaRequisitos =
		SELECT  1 AS "Prioridad"
		       	, CASE WHEN (CASE WHEN :p_Uso is null Then 1 ELSE COUNT("NombreUso") END) = 0 THEN 0 ELSE 1 END AS "Cumple"
		       	,'USO_FLUJO_CAJA_NO_EXISTE' AS "Codigo"
		       	,'NombreUso' as "CodigoDato1"
				,p_Uso as "ValorDato1"
		       	,'' as "CodigoDato2"
				,'' as "ValorDato2"				 
				,0  as "IdInterno"
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionFlujoCaja"
		WHERE "NombreUso" = :p_Uso
		UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","IdInterno" FROM :v_TablaRequisitos;
		
		
		--**********************************************************************************************
		--***********************************  ANALISIS DE DATOS  **************************************
		--**********************************************************************************************
		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   			INTO v_error_General, v_error_Negocio
	   	  FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
	   								 WHEN :v_error_Negocio > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;
	   								   									
		p_out_MovimientosRech =
	     SELECT DISTINCT "IdInterno"
	      FROM :v_TablaRequisitos
	     WHERE "Cumple" = 0;
	   							
		v_TablaCodigosMensajes =
		SELECT  "IdInterno"
			   ,"Codigo"
			   ,"CodigoDato1"
			   ,"ValorDato1"
			   ,"CodigoDato2"
			   ,"ValorDato2" 
		  FROM :v_TablaRequisitos
		 WHERE "Cumple" = 0
		 ORDER BY "Prioridad" DESC;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,v_out_ErroresDetalle);

	    p_out_ErroresDetalle =
		SELECT ED."Codigo",
			   ED."Descripcion"
		  FROM :v_out_ErroresDetalle as ED;
	END;
	
END;
