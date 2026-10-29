SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.VencimientoClaves";
-- 
--  Proposito: Servicio para vencimiento masivo de claves de usuario
--  Autor: - CHERNANDEZ
--  Fecha Creacion: 21/01/2016
--  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.VencimientoClaves" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para la asignacion del rol

	-- Parametros salida del servicio publico
	OUT p_out_cantidadDeClavesVencidas	BIGINT,
		
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
		DECLARE v_diasCambioClave	BIGINT DEFAULT 60;
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			
			--obtener cantidad de dias para cambio de clave
			SELECT 
				CAST("Valor" AS BIGINT) INTO v_diasCambioClave
			FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
			WHERE "IdEmpresa"= p_IdEmpresa and "CodigoParametro" = 'PLA.DiasCambioClave';
			
			--obtener cantidad de usuarios a vencer clave
			select 
				COUNT("IdUsuario") INTO p_out_cantidadDeClavesVencidas
			FROM "BSCL.Plataforma.BD::ACC.Usuario"
			WHERE DAYS_BETWEEN("UltimoCambioClave", CURRENT_UTCTIMESTAMP) > v_diasCambioClave AND "IdEmpresa" = p_IdEmpresa;
			
			IF p_out_cantidadDeClavesVencidas > 0 THEN 
				--vencer clave a los usuarios.
				UPDATE "BSCL.Plataforma.BD::ACC.Usuario"
					SET "DebeCambiarClave" = 'S'
				WHERE DAYS_BETWEEN("UltimoCambioClave", CURRENT_UTCTIMESTAMP) > v_diasCambioClave AND "IdEmpresa" = p_IdEmpresa;
			END IF; 
			
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;						  
			
		END;
		
	END;
END;