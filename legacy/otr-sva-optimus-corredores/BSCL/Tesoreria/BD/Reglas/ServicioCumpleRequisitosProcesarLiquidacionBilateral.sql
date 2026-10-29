
SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosProcesarLiquidacionBilateral";
GO
CREATE PROCEDURE
-- 
--  Proposito: Validar que se cumplan las condiciones para procesar la liquidacion bilateral
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 01/09/2016
--
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosProcesarLiquidacionBilateral"(

    IN p_IdEmpresa             				   BIGINT,
    IN p_FechaSistema                          NVARCHAR(10),
    IN p_FechaLiquidacion			    	   NVARCHAR(10),
	IN p_LiquidacionBilateralMueveCtaInversion NVARCHAR(1),

	IN p_LiquidacionBilateralResumen  	    TABLE  (
												 "IdAgrupacion"								BIGINT
												,"IdBolsa"									NVARCHAR(3)
												,"IdPersonaCorredoraContraparte"			BIGINT
												,"DocumentoIdentidadCorredoraContraparte" 	NVARCHAR(40)
												,"NombreCortoCorredoraContraparte"			NVARCHAR(40)
												,"NombreCompletoCorredoraContraparte"		NVARCHAR(250)																								
												
												,"CodigoInscripcionCorredoraContraparte"    NVARCHAR(40)																								
												,"IdMonedaLiquidacion"						NVARCHAR(3)
												,"IdCondicionDeLiquidacion"         		NVARCHAR(2)
												,"CodigoCondicionDeLiquidacion"     		NVARCHAR(40)
												,"IdClasificacionTipoInstrumento" 			NVARCHAR(3)
												,"IdTipoInstrumento"						NVARCHAR(3)
												,"AccionTipoOperacion"      				NVARCHAR(40)
												,"IdComprobanteTesoreria"           		BIGINT
												,"TotalMontoCompra" 						DECIMAL(26,8)
												,"TotalMontoVenta"  						DECIMAL(26,8)
											),    


	OUT p_out_GruposRech	            TABLE("IdAgrupacion" BIGINT),
	
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

		DECLARE v_TablaCodigosMensajes	TABLE  ("CodigoMensaje" NVARCHAR(100), "CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520));

		DECLARE v_TablaRequisitos		TABLE  ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100), 
												"CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520), "IdAgrupacion"  BIGINT);	
												
											   												

		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN						
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		
		v_TablaRequisitos = SELECT  0 AS "Prioridad",0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato",'' AS "ValorDato", 0 AS "IdAgrupacion"
							FROM dummy WHERE (1=2);
							  
		
		
		
		-----------------------------------------------------------
		--  VALIDACIONES BASICAS
		-----------------------------------------------------------

		-- Valida que fecha de sistema sea válida
		v_TablaRequisitos =
		SELECT 	 1 as "Prioridad"
		       	,CASE 
		       		WHEN ("BSCL.Plataforma.BD.Utilitarios::EsFecha" (p_FechaSistema) = 'N') OR (p_FechaSistema = '0001-01-01') THEN 0 
		       		ELSE 1 
				 END as "Cumple" 
		       	,'FECHA_SISTEMA_INVALIDA' as "CodigoError"
		       	,'Fecha' as "CodigoDato"
		       	,:p_FechaSistema as "ValorDato"		       	
		       	,0 as "IdAgrupacion"
		FROM DUMMY
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdAgrupacion" FROM :v_TablaRequisitos;





		-- Valida que vengan transacciones bilaterales para procesar
		v_TablaRequisitos =
		SELECT 	 1 AS "Prioridad"
		       	,CASE 
		       		WHEN COUNT("IdAgrupacion") < 1 THEN 0 
		       		ELSE 1 
				 END AS "Cumple" 
		       	,'NO_EXISTEN_TRANSACCIONES_BILATERALES_SIN_LIQUIDAR_PARA_PROCESAR' AS "CodigoError"
		       	,'' AS "CodigoDato"
		       	,'' AS "ValorDato"
		       	,0  AS "IdAgrupacion"
		FROM :p_LiquidacionBilateralResumen
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdAgrupacion" FROM :v_TablaRequisitos;


		
		-----------------------------------------------------------
		--  VALIDACIONES NEGOCIO
		-----------------------------------------------------------
		
		-- Valida que la corredora contraparte tenga asociada una persona y un cliente(Necesario para cta inversión)
		v_TablaRequisitos =
		SELECT 	2 AS "Prioridad"
		       	,CASE 
			       	WHEN (COALESCE(LBR."IdPersonaCorredoraContraparte",0) = 0) THEN 0
		       		WHEN (COALESCE(SN."IdCliente",0) = 0 AND :p_LiquidacionBilateralMueveCtaInversion='S') THEN 0
		       		ELSE 1
				 END AS "Cumple"
		       	,CASE
			       	WHEN (COALESCE(LBR."IdPersonaCorredoraContraparte",0) = 0) THEN 'CORREDORA_CONTRAPARTE_SIN_PERSONA_ASOCIADA'
		       		WHEN (COALESCE(SN."IdCliente",0) = 0 AND :p_LiquidacionBilateralMueveCtaInversion='S') THEN 'CORREDORA_CONTRAPARTE_NO_REGISTRADA_COMO_CLIENTE' -- solo si mueve cta inversion, ya que cta inv trabaja solo con clientes
		       		ELSE ''
				 END AS "CodigoError"				 
		       	,'NombreCorredoraContraparte' AS "CodigoDato"
		       	,CAST(LBR."NombreCortoCorredoraContraparte" as NVARCHAR(40)) AS "ValorDato"		       	
                ,LBR."IdAgrupacion"
		FROM :p_LiquidacionBilateralResumen AS LBR
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS SN ON (SN."IdPersona" = LBR."IdPersonaCorredoraContraparte" AND SN."IdEmpresa" = :p_IdEmpresa)
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdAgrupacion" FROM :v_TablaRequisitos;



		-- Valida que exista preferencia de pago de la corredora para liquidación bilateral(según tipo de movimiento y moneda)
		v_TablaRequisitos =
		SELECT 	2 AS "Prioridad"
		       	,CASE
			       	WHEN (COALESCE(PRE."IdPreferenciasPagoCorredor",0) = 0) THEN 0
		       		ELSE 1
				 END AS "Cumple"
		       	,CASE
			       	WHEN (COALESCE(PRE."IdPreferenciasPagoCorredor",0) = 0) THEN 'PREFERENCIA_PAGO_LIQUIDACION_BILATERAL_NO_EXISTE'
		       		ELSE ''
				 END AS "CodigoError"				 
		       	,'Codigo' AS "CodigoDato"
		       	,CAST(LBR."IdAgrupacion" as NVARCHAR(40)) AS "ValorDato"		       	
                ,LBR."IdAgrupacion"
		FROM :p_LiquidacionBilateralResumen AS LBR
		LEFT JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarPreferenciaPagoCorredor" AS PRE ON 
		(
				    PRE."IdMoneda" = LBR."IdMonedaLiquidacion"
				AND PRE."IdTipoMovimiento" = (CASE WHEN (LBR."AccionTipoOperacion" = 'COMPRA') THEN 'MOVEGR' ELSE 'MOVING' END)
				AND PRE."IdConcepto" = 'LIQBIL'
				AND PRE."IdTipoMovimiento" IN ('MOVING','MOVEGR')
				AND PRE."NombreEntidad" = 'TES.LiquidacionBilateral'
				AND PRE."IdEstado" = 'ACT'
				AND PRE."IdEmpresa" = :p_IdEmpresa
		)
		UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdAgrupacion" FROM :v_TablaRequisitos;
				

		
		
		
		-----------------------------------------------------------
		--  ANALISIS DE DATOS
		-----------------------------------------------------------

	   	SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" > 1 THEN 1 END)
	   			INTO v_error_General, v_error_Negocio
	   	FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0 THEN 'RECH'
	   								 WHEN :v_error_Negocio > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;


	    p_out_GruposRech =
	    SELECT DISTINCT "IdAgrupacion"
	    FROM :v_TablaRequisitos
	    WHERE "Prioridad" > 1
	    AND "Cumple" = 0;	   



	    v_TablaCodigosMensajes =
		SELECT 	"CodigoError" AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato" 
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0
		ORDER By "Prioridad" DESC;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes, p_out_ErroresDetalle);


		p_out_ErroresDetalle =
		SELECT ED."Codigo",
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion" || ' (' || TR."ValorDato" || ')')
					ELSE ED."Descripcion"
				END as "Descripcion"
		FROM :p_out_ErroresDetalle as ED
		INNER JOIN :v_TablaRequisitos as TR ON TR."CodigoError" = ED."Codigo"
		WHERE "Cumple" = 0;

	    
    END;	
END;