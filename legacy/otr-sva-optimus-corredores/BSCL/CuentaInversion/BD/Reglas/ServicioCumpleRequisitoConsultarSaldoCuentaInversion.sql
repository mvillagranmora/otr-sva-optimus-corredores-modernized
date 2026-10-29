SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoConsultarSaldoCuentaInversion";

CREATE PROCEDURE
-- 
--  Proposito: Agrupador de las validaciones basicas para Empresa, Modulo y Usuario
--  Autor: NRM - BCS
--  Fecha Creacion: 28/07/2015  
--     
"BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoConsultarSaldoCuentaInversion" (
	-- Parametros entrada
	IN  p_IdEmpresa					BIGINT,
	IN  p_FechaDesde				NVARCHAR(10),
    IN  p_FechaHasta				NVARCHAR(10),
    IN  p_CuentaInversionAConsultar	TABLE ( "IdInterno" 		BIGINT,
    										"IdEjecutivo"		BIGINT,
											"IdPortafolio"		BIGINT,
											"IdMoneda"			NVARCHAR(3)),    
    
	-- Salidas para mensajes de errores
	OUT p_out_MovimientosRech		TABLE ( "IdInterno" 		BIGINT),
	OUT p_out_ErroresDetalle		TABLE ( "Codigo"	 		NVARCHAR(100), 
											"Descripcion" 		NVARCHAR(5000)),
	OUT p_out_resultadoRegla		NVARCHAR(40)   	--OK=Cumple todos los requisitos,		
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
	DECLARE v_erroroutmsg  			NVARCHAR(5000);
	DECLARE v_ErrorNoData  			NVARCHAR(100); 	
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
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
	
	
	BEGIN
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio			BIGINT;
		DECLARE v_TablaCodigosMensajes	TABLE ("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),
												"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
		DECLARE v_TablaRequisitos     	TABLE  ("Prioridad" INT,"Cumple" BIGINT, "Codigo" NVARCHAR(100), 
												"CodigoDato1" NVARCHAR(100), "ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100), 
												"ValorDato2" NVARCHAR(520),"IdInterno" BIGINT);
				
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
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
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaDesde) = 'N') OR (p_FechaDesde = '0001-01-01') THEN 'Fecha'
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaHasta) = 'N') OR (p_FechaHasta = '0001-01-01') THEN 'Fecha'
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
				,0 as "IdInterno"
		  FROM DUMMY
		 UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","IdInterno" FROM :v_TablaRequisitos;
		 
		
		v_TablaRequisitos =
		 SELECT  1 AS "Prioridad"
		       	,CASE WHEN COUNT(1) = 0 THEN 0 ELSE 1 END AS "Cumple"
		       	,'NO_LLEGAN_MOVIMIENTOS_CONSULTA_SALDO_CUENTA_INVERSION' AS "Codigo"
		       	,'' AS "CodigoDato1"
		       	,'' AS "ValorDato1"
		       	,'' AS "CodigoDato2"
		       	,'' AS "ValorDato2"
				,0 as "IdInterno"
		   FROM :p_CuentaInversionAConsultar
		  UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","IdInterno" FROM :v_TablaRequisitos;		 
		
		--**********************************************************************************************
		--***************************************  NEGOCIO  ********************************************
		--**********************************************************************************************

		v_TablaRequisitos =
		 SELECT	 2 AS "Prioridad"
				,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN 0
		       		  ELSE 1 
				 END AS "Cumple"
				,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN 'CUENTA_INVERSION_NO_EXISTE'
		       		  ELSE ''
				 END AS "Codigo"
				,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN 'IdPortafolio'
		       		  ELSE ''
				 END AS "CodigoDato1"
				,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN CAST(CIC."IdPortafolio" as NVARCHAR(520))
					  ELSE '' 
				 END AS "ValorDato1"
				,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN 'IdMoneda'
		       		  ELSE ''
				 END AS "CodigoDato2"
				,CASE WHEN COALESCE(CI."IdCuentaInversion",0) = 0 THEN CIC."IdMoneda"
					  ELSE '' 
				 END AS "ValorDato2"
				,"IdInterno"
		   FROM :p_CuentaInversionAConsultar AS CIC 
		   LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI  
		 		  ON CI."IdEmpresa"	  		= :p_IdEmpresa		 		
		         AND CI."IdPortafolio" 		= COALESCE(CIC."IdPortafolio", CI."IdPortafolio")
		         AND CI."IdMoneda"     		= COALESCE(CIC."IdMoneda", CI."IdMoneda")
		  UNION ALL SELECT "Prioridad","Cumple","Codigo","CodigoDato1","ValorDato1","CodigoDato2","ValorDato2","IdInterno" FROM :v_TablaRequisitos;
		
			
		--select * from :v_TablaRequisitos; --borrar	
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
	     SELECT "IdInterno"
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
