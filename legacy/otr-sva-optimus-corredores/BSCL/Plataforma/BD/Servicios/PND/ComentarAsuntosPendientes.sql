SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::PND.ComentarAsuntosPendientes";
-- 
--  Proposito: Servicio para comentar asuntos pendientes masivo
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 10/08/2016
--  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::PND.ComentarAsuntosPendientes" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para comentarios masivos
	IN p_IdEmpleado					BIGINT,
	IN p_Comentarios				TABLE(
									"IdDetalle"		BIGINT,
									"Comentario"    NVARCHAR(200)
									),
		
	--Parametros de salida

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
BEGIN


	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;



	BEGIN
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		
		-----------------------------------------------------
		--LOGICA DE NEGOCIO
		-----------------------------------------------------		
		UPDATE "BSCL.Plataforma.BD::PND.DetalleAsuntoPendiente" DAP
		SET DAP."Observaciones" = C."Comentario"
		FROM "BSCL.Plataforma.BD::PND.DetalleAsuntoPendiente" DAP, :p_Comentarios C
		WHERE DAP."IdDetalleAsuntoPendiente" = C."IdDetalle" AND "IdEmpleado" = p_IdEmpleado AND "IdEmpresa" = p_IdEmpresa ;

	END;
END;
