SET SCHEMA "BSCL";


DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::LIM.ModificarEstadoRegistroEvaluacionLimite"; 


CREATE PROCEDURE 
-- 
--  Proposito: Servicio Interno que modifica el estado de la evaluación 
--  enviados desde las entidades del Negocio o Autorizaciones.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 04/05/2016
--  
"BSCL.Plataforma.BD.Servicios::LIM.ModificarEstadoRegistroEvaluacionLimite" (
	-- Parametros entrada comunes en todos los servicios publicos

	IN  p_IdEmpresa					BIGINT,						--ID Empresa destino
	IN  p_IdUsuario					BIGINT,						--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  				--ID del Modulo que lo invoca	
	-- Parametros entrada del servicio publico
	IN 	p_IdEvaluacionLimite						BIGINT,  
	IN  p_IdEstadoAutorizacion						NVARCHAR(3)  DEFAULT '',

		
	-- Parametros salida del servicio publico
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
 
	DECLARE v_errorhashmsgtable TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg 		NVARCHAR(520);
	DECLARE v_SecuenciaEvento	BIGINT;	  
	DECLARE v_out_Resultado 	NVARCHAR(1);
    DECLARE v_out_Mensaje 		NVARCHAR(520);
	
	DECLARE SALIDA_CONTROLADA CONDITION; 	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			p_out_ErroresDetalle = SELECT '' AS "Codigo", ::SQL_ERROR_MESSAGE AS "Descripcion" FROM dummy;
			--Error INTerno no manejado  (El log será captado por la capa de XS control)		
			--RESIGNAL; v_MensajeError
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	
	BEGIN	
		--------------------
	
		UPDATE "BSCL.Plataforma.BD::LIM.EvaluacionLimites"
			SET "IdEstadoAutorizacion" = p_IdEstadoAutorizacion
		WHERE "IdEvaluacionLimite" = :p_IdEvaluacionLimite
		AND "IdEstadoAutorizacion"='PEN'; --Solo Pendientes por autorizar/rechazar
		
	
	   
	--------------------
	--FIN SP 		
	--------------------
	END;

END;

