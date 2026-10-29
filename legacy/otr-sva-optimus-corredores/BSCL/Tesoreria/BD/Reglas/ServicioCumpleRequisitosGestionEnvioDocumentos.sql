SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionEnvioDocumentos";

CREATE PROCEDURE
-- 
--  Proposito: valida datos para hacer una actualizacion de envio de documentos
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 13/02/2017
--     	
"BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosGestionEnvioDocumentos"(

	IN p_IdEmpresa                      BIGINT,
    IN p_TipoEjecucion					NVARCHAR(3),	-- EST: Cambio de estado(aceptado, devuelto)
														-- ING: Crear un nuevo envio de documentos	
		
	IN p_EnvioDocumento					TABLE (
												"IdEnvioDocumento"				BIGINT,    -- Para ING(crear ED) este número es sólo referencial
												"IdCaja"						BIGINT,	
												"IdMedioDePago" 				NVARCHAR(3),
												"IdMoneda"  					NVARCHAR(3),
												"IdEstado"  					NVARCHAR(3)
										      ),

	IN p_EnvioDocumentoDetalle			TABLE (
												"IdEnvioDocumento"				BIGINT,
												"IdEnvioDocumentoDetalle" 		BIGINT,
												"IdComprobanteFormaDePago"		BIGINT,
												"IdEstado"                      NVARCHAR(3)
										      ),

	OUT p_out_Resultado					TABLE (
												"IdEnvioDocumento"				BIGINT,
												"IdEnvioDocumentoDetalle" 		BIGINT,
												"IdComprobanteFormaDePago" 		BIGINT,
			   								    "TipoEjecucion" 				NVARCHAR(3),
			   								    "Codigo"						NVARCHAR(100),
			   								    "Resultado"						NVARCHAR(4))	-- OK   = Cumple todos los requisitos
																								-- ADVE = 
						                                                						-- RECH = Alguna de las reglas detienen la operacion
																								-- RAUT = Requiere autorizacion
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
AS
BEGIN

	------------------------------------------------
	-- VALIDACIONES Y REGLAS
	------------------------------------------------
	v_MatrizValidaciones = 
	SELECT
			 ED."IdEnvioDocumento"
			,ED."IdCaja"
			,ED."IdMedioDePago"
			,ED."IdMoneda"
			,ED."IdEstado" AS "IdEstadoEnvioDocumento"
			,EDD."IdEnvioDocumentoDetalle"
			,EDD."IdComprobanteFormaDePago"
			,EDD."IdEstado" AS "IdEstadoEnvioDocumentoDetalle"
												
			-- Validaciones a nivel de encabezado de Envio Documentos
			,1 As "TIPO_EJECUCION_NO_VALIDA"
			,CASE WHEN :p_TipoEjecucion = 'ING' THEN 1 ELSE 0 END as "MEDIO_DE_PAGO_NO_CONFIGURADO_PARA_ENVIO_DE_DOCUMENTOS"

			-- Validaciones a nivel de detalle de Envio Documentos
			,1 As "COMPROBANTE_FORMA_DE_PAGO_NO_EXISTE" 
			,CASE WHEN :p_TipoEjecucion = 'ING' THEN 1 ELSE 0 END as "DOCUMENTO_NO_HA_SIDO_ENVIADO_A_BOVEDA"                    -- los documentos no pueden ser enviados sin antes entrar a boveda (EBV)
			,CASE WHEN :p_TipoEjecucion = 'EST' THEN 1 ELSE 0 END as "DOCUMENTO_SE_ENCUENTRA_DEVUELTO_NO_PUEDE_SER_MODIFICADO"  -- Los documentos devueltos no pueden modificarse, según flujo deben ser enviados nuevamente
	FROM :p_EnvioDocumento AS ED
	INNER JOIN :p_EnvioDocumentoDetalle AS EDD ON(ED."IdEnvioDocumento" = EDD."IdEnvioDocumento");
	
	
	
	--1) Validaciones a nivel de encabezado de "Envio Documentos"
	v_Validaciones_1 = 	
	SELECT
			 MV."IdEnvioDocumento"
		    ,MV."IdEnvioDocumentoDetalle"
		    ,MV."IdComprobanteFormaDePago"
    	    ,:p_TipoEjecucion AS "TipoEjecucion"    	
			,CASE 
				WHEN ("TIPO_EJECUCION_NO_VALIDA" = 1 AND :p_TipoEjecucion <> 'EST'  AND :p_TipoEjecucion <> 'ING') THEN 'RECH' 
				ELSE 'OK' 
			END AS "TIPO_EJECUCION_NO_VALIDA"			
			,CASE 
				WHEN ("MEDIO_DE_PAGO_NO_CONFIGURADO_PARA_ENVIO_DE_DOCUMENTOS" = 1 AND COALESCE(MDP."IdMedioDePago",'') = '') THEN 'RECH' 
				ELSE 'OK' 
			END AS "MEDIO_DE_PAGO_NO_CONFIGURADO_PARA_ENVIO_DE_DOCUMENTOS"			
	 FROM :v_MatrizValidaciones AS MV
	 LEFT JOIN "BSCL.Tesoreria.BD::TES.MedioDePagoMonedaTesoreria" AS MDP ON
	 (
			    MDP."IdMedioDePago" = MV."IdMedioDePago"
			AND MDP."IdMoneda"      = MV."IdMoneda"
			AND MDP."IdEmpresa"     = :p_IdEmpresa
	 );
	  


	--2) Validaciones a nivel de detalle de "Envio Documentos"
	v_Validaciones_2 =	
	SELECT 
		 	MV."IdEnvioDocumento"
		   ,MV."IdEnvioDocumentoDetalle"
		   ,MV."IdComprobanteFormaDePago"						
    	   ,:p_TipoEjecucion AS "TipoEjecucion"
		   ,CASE 
		   		WHEN "COMPROBANTE_FORMA_DE_PAGO_NO_EXISTE" = 1 AND COALESCE(CFP."IdComprobanteFormaDePago",0)=0 THEN 'RECH' 
		   		ELSE 'OK' 
		   	END AS "COMPROBANTE_FORMA_DE_PAGO_NO_EXISTE"
		   ,CASE 
		   		WHEN "DOCUMENTO_SE_ENCUENTRA_DEVUELTO_NO_PUEDE_SER_MODIFICADO" = 1 AND COALESCE(EDD."IdEstado",'') = 'DEV' THEN 'RECH' 
		   		ELSE 'OK' 
		   	END AS "DOCUMENTO_SE_ENCUENTRA_DEVUELTO_NO_PUEDE_SER_MODIFICADO"
		   
	  FROM :v_MatrizValidaciones AS MV	  
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" AS CFP ON (CFP."IdComprobanteFormaDePago" = MV."IdComprobanteFormaDePago")
	  LEFT JOIN "BSCL.Tesoreria.BD::TES.EnvioDocumentoDetalle" AS EDD ON (EDD."IdEnvioDocumentoDetalle" = MV."IdEnvioDocumentoDetalle");
										      


	p_out_Resultado =
	 SELECT "IdEnvioDocumento",
			"IdEnvioDocumentoDetalle",
			"IdComprobanteFormaDePago",
			"TipoEjecucion",
			"Codigo",
			"Resultado"
	   FROM (
			 	SELECT
					"IdEnvioDocumento",
					"IdEnvioDocumentoDetalle",
					"IdComprobanteFormaDePago",
	   				"TipoEjecucion",
			 		COALESCE(
			 			"1" ||"TIPO_EJECUCION_NO_VALIDA",
			 			"2" ||"MEDIO_DE_PAGO_NO_CONFIGURADO_PARA_ENVIO_DE_DOCUMENTOS"
			 		) AS "Resultado",
			 		COALESCE(
			 			"1" ||'TIPO_EJECUCION_NO_VALIDA',
			 			"2" ||'MEDIO_DE_PAGO_NO_CONFIGURADO_PARA_ENVIO_DE_DOCUMENTOS'
			 		) AS "Codigo"
			   	FROM :v_Validaciones_1,"BSCL.Plataforma.BD::PLA.Identidad"
			 )
	  WHERE "Resultado" IS NOT NULL

	  UNION ALL
	  
	 SELECT "IdEnvioDocumento",
			"IdEnvioDocumentoDetalle",
			"IdComprobanteFormaDePago",
			"TipoEjecucion",
			"Codigo",
			"Resultado"
	   FROM (
			 	SELECT
 					"IdEnvioDocumento",
					"IdEnvioDocumentoDetalle",
					"IdComprobanteFormaDePago",
	   				"TipoEjecucion",	   								   					
			 		COALESCE(
						"1" ||"COMPROBANTE_FORMA_DE_PAGO_NO_EXISTE",
					    "2" ||"DOCUMENTO_SE_ENCUENTRA_DEVUELTO_NO_PUEDE_SER_MODIFICADO"
			 		) AS "Resultado",
			 		COALESCE(
			 			"1" ||'COMPROBANTE_FORMA_DE_PAGO_NO_EXISTE',
					    "2" ||'DOCUMENTO_SE_ENCUENTRA_DEVUELTO_NO_PUEDE_SER_MODIFICADO'
			 		) AS "Codigo"
			   	FROM :v_Validaciones_2,"BSCL.Plataforma.BD::PLA.Identidad"
			   )
	  WHERE "Resultado" IS NOT NULL;

END;
