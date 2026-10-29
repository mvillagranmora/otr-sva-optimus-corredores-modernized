SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.ModificarPlantillaEmpleadoDetalle";

CREATE PROCEDURE  
-- 
--  Proposito: Creacion de Plantilla de Empleado. Recibe cabecera y detalle.
--  Autor: JGARCES - BCS
--  Fecha Creacion: 01/02/2016
--  
"BSCL.Plataforma.BD.Servicios::COM.ModificarPlantillaEmpleadoDetalle" (
	-- Parametros entrada comunes en todos los servicios publicos

	IN  p_IdEmpresa							BIGINT,			--ID Empresa destino
	IN  p_IdUsuario							BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo							NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.	

	IN p_IdPlantillaEmpleado			    NVARCHAR(40),	
	IN p_IdPlantillaEmpleadoDetalle			    NVARCHAR(40),	
	IN p_IdTipoProducto						NVARCHAR(4),
	IN p_IdTipoOperacion					NVARCHAR(6),
	IN p_PermiteRuteo						NVARCHAR(1),
	IN p_MontoMaximoOperacion				DECIMAL(26,8),
	IN	p_Descuento							DECIMAL(26,8),
	IN	p_IdEstado					   		NVARCHAR(3),
	

	
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

		DECLARE v_FechaIngreso	SECONDDATE;	-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'

		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
/*
			SELECT COUNT(1) INTO v_cantidadPersona from :p_Persona;
			if :v_cantidadPersona != 1 then
				v_hashmsg = select 'TipoEntidad' as "Codigo", 'persona' as "Valor" from dummy;			     		
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('SOLO_REGISTRO_UNICO',:v_hashmsg, :v_erroroutmsg);		
 				p_out_ErroresDetalle =  select 'SOLO_REGISTRO_UNICO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
								select * from :p_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			end if;				
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;		
*/		
		END;
		
		-----------------------------------------------------
		--Validaciones via Motor (solo complejas)
		--(Normalmente no deberia usarse este bloque de codigo ) 
		-----------------------------------------------------		

			
		--------------------
		--Reglas Negocio 
		--------------------		
		
		
			
				
				
		--------------------
		--Negocio 
		--------------------
		BEGIN
					
			--CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			
			UPDATE "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" 
				SET "IdTipoProducto" = p_IdTipoProducto,
				"IdTipoOperacion" = p_IdTipoOperacion,
				"PermiteRuteo" = p_PermiteRuteo,
				"PorcentajeDescuentoComision" = p_Descuento,
				"MontoMaximoOperacion" = p_MontoMaximoOperacion,
				
				"IdEstado" = p_IdEstado
			WHERE "IdDetallePlantillaEmpleado" = p_IdPlantillaEmpleadoDetalle
			and "IdPlantillaEmpleado" = p_IdPlantillaEmpleado;
			
			
			
			

			-- TODO EJECUTADO HASTA AQUI. SE RETORNA 1.
			select 1 into p_out_RefId from dummy;
					
		END;		
		--------------------
		--Normativo 
		--------------------
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;