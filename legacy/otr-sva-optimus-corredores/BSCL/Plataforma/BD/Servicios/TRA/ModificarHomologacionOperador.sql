SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::TRA.ModificarHomologacionOperador";

CREATE PROCEDURE  
-- 
--  Proposito: Modificar Homologacion Operador
--  Autor: JGARCES - BCS
--  Fecha Creacion: 09/02/2016
--  
"BSCL.Plataforma.BD.Servicios::TRA.ModificarHomologacionOperador" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa							BIGINT,			--ID Empresa destino
	IN  p_IdUsuario							BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo							NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_simularOperacion					NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)
	IN  p_UsaReglasNegocio	 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.	

	IN p_IdHomologacionOperador				BIGINT,
	IN p_CodigoOperador						NVARCHAR(40)	DEFAULT '',	
	IN p_IdBolsa							NVARCHAR(100)	DEFAULT '',		
	IN p_CodigoHomologacion					NVARCHAR(40)	DEFAULT '',			
	IN p_IdEstado							NVARCHAR(3)		DEFAULT 'ACT',	

	-- Parametros salida del servicio publico
	OUT p_out_RefId bigint,	
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
			call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
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
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
	
		END;
		
		-----------------------------------------------------
		--Validaciones via Motor (solo complejas) 
		-----------------------------------------------------		

		--------------------
		--Reglas Negocio 
		--------------------				
		
		--Solo simulacion del servicio
		if p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;					
				
		--------------------
		--Negocio 
		--------------------
		BEGIN
				
			UPDATE "BSCL.Plataforma.BD::TRA.HomologacionOperador" SET
				"CodigoOperador" = :p_CodigoOperador,
				"IdBolsa" = :p_IdBolsa,			
				"CodigoHomologacion" = :p_CodigoHomologacion,
				"IdEstado" = :p_IdEstado,
				"FechaModificacion" = CURRENT_UTCTIMESTAMP,
				"IdUsuarioModifica" = :p_IdUsuario
			WHERE "IdHomologacionOperador" = :p_IdHomologacionOperador;
					
			-- TODO EJECUTADO HASTA AQUI. SE RETORNA 1.
			IF(::ROWCOUNT>0) THEN
				select 1 into p_out_RefId from dummy;
			END IF;
					
		END;		
		--------------------
		--Normativo 
		--------------------
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
