SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::TRA.ModificarConfiguracionOperador";

CREATE PROCEDURE  
-- *********************************************************************************  
--  Proposito: Crear Configuracion del Operador
--  Autor: JGARCES - BCS
--  Fecha Creacion: 09/02/2016
-- ********************************************************************************* 
--  Proposito: [CELCONDOR-688] - Se agrega el ID del Libro de Ordenes
--  Autor: RAM - BCS
--  Fecha Creacion: 25/11/2019
-- *********************************************************************************
--  
"BSCL.Plataforma.BD.Servicios::TRA.ModificarConfiguracionOperador" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_IdEmpresa						BIGINT,			--ID Empresa destino
	IN p_IdUsuario						BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo						NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_simularOperacion				NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 			NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.	
    IN p_IdLibroOrden                   BIGINT,         -- ID del libro de ordenes [CELCONDOR-688] 
	IN p_CodigoOperador					NVARCHAR(10),
	IN p_IdCorredora					BIGINT,
	IN p_ReservaTransaccion				NVARCHAR(1)		DEFAULT	'N',	
	IN p_UtilizaPortafolio				NVARCHAR(1)		DEFAULT 'N',	
	IN p_IdPortafolio					BIGINT			DEFAULT 0,		
	IN p_IdEstado						NVARCHAR(3)		DEFAULT 'ACT',	
	
	IN p_detalle			TABLE (
								"IdTipoInstrumento"			NVARCHAR(3),	
								"AsignacionAuto"			NVARCHAR(1),	
								"ComprobanteAuto"			NVARCHAR(1),	
								"MovimientoFacturarAuto" 	NVARCHAR(1),
								"DetalleExiste" 			NVARCHAR(1)	
							),

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
		
		--Solo simulacion del servicio
		if p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;					
				
		--------------------
		--Negocio 
		--------------------
		BEGIN
				
			UPDATE "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" SET 
				"IdCorredora" = :p_IdCorredora,
				"UtilizaPortafolio" = :p_UtilizaPortafolio,
				"IdPortafolio" = :p_IdPortafolio,
				"ReservaTransaccion" = :p_ReservaTransaccion,
				"IdEstado" = :p_IdEstado,
				"FechaModificacion" = CURRENT_UTCTIMESTAMP,
				"IdUsuarioModifica" = :p_IdUsuario,
				"IdEmpresa" = :p_IdEmpresa,
				"IdLibroOrdenes" = p_IdLibroOrden       --	[CELCONDOR-688]			
			WHERE "CodigoOperador" = :p_CodigoOperador;
						
			IF(::ROWCOUNT>0) THEN
			
					-- INSERTAR LOS DETALLES		
				INSERT INTO "BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador" ( 
					"CodigoOperador",
					"IdTipoInstrumento",
					"AsignacionAuto",
					"ComprobanteAuto",
					"MovimientoFacturarAuto",
					"IdEstado",
					"FechaCreacion",
					"FechaModificacion",
					"IdUsuarioCrea",
					"IdUsuarioModifica",
					"IdEmpresa" )
				SELECT 
					:p_CodigoOperador,
					"IdTipoInstrumento",
					"AsignacionAuto",
					"ComprobanteAuto",
					"MovimientoFacturarAuto",
					'ACT',
					CURRENT_UTCTIMESTAMP,
					CURRENT_UTCTIMESTAMP,
					:p_IdUsuario,
					:p_IdUsuario,
					:p_IdEmpresa
				FROM :p_detalle WHERE "DetalleExiste" = 'N';
	
				-- ACTUALIZAR LOS DETALLES(En este modo de creacion no se deberían actualizar)
				UPDATE DCO
				SET
					DCO."AsignacionAuto" 			= DET."AsignacionAuto",
					DCO."ComprobanteAuto" 			= DET."ComprobanteAuto",
					DCO."MovimientoFacturarAuto" 	= DET."MovimientoFacturarAuto",
					DCO."IdEstado"					= :p_IdEstado,
					DCO."FechaModificacion"			= CURRENT_UTCTIMESTAMP,
					DCO."IdUsuarioModifica"			= :p_IdUsuario,
					DCO."IdEmpresa"					= :p_IdEmpresa
				FROM 
					"BSCL.Plataforma.BD::TRA.DetalleConfiguracionOperador" as DCO,
					:p_detalle as DET
				WHERE 
					:p_CodigoOperador = DCO."CodigoOperador" AND DET."IdTipoInstrumento" = DCO."IdTipoInstrumento";
				

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