SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CER.RecuperarCertificadoCliente";
-- 
--  Proposito: Servicio para la recuperacion de un certificado de cliente
--  Autor: - CHERNANDEZ
--  Fecha Creacion: 13/09/2016
--  
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CER.RecuperarCertificadoCliente" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para recuperar la informacion del certificado
	IN  p_IdCertificadoCliente	BIGINT,

	-- Parametros salida del servicio publico
		
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
		DECLARE v_ExisteCertificado	BIGINT;
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN			
			--Verificar que el certificado exista
			select COUNT("IdCertificadoCliente") INTO v_ExisteCertificado 
			FROM "BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente" 
			where "IdCertificadoCliente" = p_IdCertificadoCliente and "IdEmpresa" = p_IdEmpresa;
	
			IF v_ExisteCertificado < 1 THEN
				p_out_ErroresDetalle =  select 'UI_NO_EXISTE_CERTIFICADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			END IF;

			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;						  
			
		END;
		------------------------------------------
		--LOGICA DE NEGOCIO
		------------------------------------------

		SELECT "JsonData"
		FROM "BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente"
		WHERE "IdCertificadoCliente" = p_IdCertificadoCliente and "IdEmpresa" = p_IdEmpresa;
					
	END;
END;
