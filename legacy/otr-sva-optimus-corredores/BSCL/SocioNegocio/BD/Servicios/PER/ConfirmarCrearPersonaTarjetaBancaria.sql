SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarCrearPersonaTarjetaBancaria";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a las tarjetas de una persona.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 28-12-2015
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarCrearPersonaTarjetaBancaria" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	
	
	IN 	p_IdTarjetaBancaria								BIGINT,	
	IN  p_IdEstadoAutorizacion						NVARCHAR(3),
	
	-- Parametros salida del servicio publico
	--OUT p_out_RefId 								BIGINT,
			
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
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
		--------------------
		--Declare Variables
		--------------------
			DECLARE v_IdEntidadOrigen								NVARCHAR(40);
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'TARJETABANCARIA';
			--variables Logs
			DECLARE v_fechaHoraSistema 								NVARCHAR(40);
			DECLARE V_subregistro 									NVARCHAR(40):= 'TARJETABANCARIA';
			DECLARE v_NombreProceso									NVARCHAR(100):='Confirmar Ingreso Tarjeta Bancaria';
			DECLARE v_idPersona 									BIGINT;
			DECLARE v_nombrePersona 								NVARCHAR(200)='';
			DECLARE v_documentoIdentidad 							NVARCHAR(40)='';
			DECLARE v_tipoDocumentoIdentidad 						NVARCHAR(40)='';
			DECLARE v_numeroTarjetaBancaria 						NVARCHAR(40):='';
			DECLARE v_nombreBanco									NVARCHAR(100):='';
			DECLARE v_tipoTarjeta									NVARCHAR(100):='';
			DECLARE v_codigoVerificacion							BIGINT:=0;
			DECLARE v_FechaVencimiento								NVARCHAR(40):='';
			DECLARE v_vacio 										NVARCHAR(3) := '';

			--Variables detalle Logs
 			DECLARE v_DetalleLog 									TABLE (
																    "SubRegistro" nvarchar(40),
																    "CampoRegistro" nvarchar(40),
																    "ValorAntiguo" nvarchar(250),
																    "ValorNuevo" nvarchar(250)
															 		);
		
			--------------------
		--Validaciones
		--------------------
	--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			
			v_IdEntidadOrigen:=CAST (p_IdTarjetaBancaria AS NVARCHAR(40));
			
			
			UPDATE "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
			SET "IdEstadoAutorizacion"=p_IdEstadoAutorizacion
			WHERE "IdTarjetaBancaria"=p_IdTarjetaBancaria;
	
			
			--Se termina el flujo con la actualizacion del registro de solicitud.
/*
			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambio" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_IdEntidadOrigen		=>:v_IdEntidadOrigen,
				p_IdTipoEntidad			=>:v_TipoEntidad,
				p_IdEstadoAutorizacion	=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador			=>:p_out_ErroresContador,
				p_out_ErroresDetalle			=>:p_out_ErroresDetalle	
			);
*/
		--INICIO LOG
			BEGIN 

				SELECT  
					PER."IdPersona" AS "IdPersona"
					,CASE WHEN LENGTH(PER."NombreCompleto") > 100 THEN LEFT((PER."NombreCompleto"),100) ELSE PER."NombreCompleto" END AS "Nombres"
					,COALESCE(PER."DocumentoIdentidad",'') 
					,COALESCE(PER."TipoDocumentoIdentidad",'')
					,COALESCE(TARJBAN."NumeroTarjetaBancaria" , '') 
					,COALESCE(BAN."NombreBanco", '') 																AS "Banco"
					,COALESCE(TIPTARJ."NombreTipoTarjetaBancaria", '') 												AS "TipoTarjetaBancaria"
					,COALESCE(TARJBAN."CodigoVerificacion", 0) 														
					,TO_NVARCHAR(COALESCE(TARJBAN."FechaVencimiento", '0001-01-01'),'YYYY-MM-DD') 					AS "Fecha"
				INTO 
					v_idPersona,
					v_nombrePersona,
					v_documentoIdentidad,
					v_tipoDocumentoIdentidad,
					v_numeroTarjetaBancaria,
					v_nombreBanco,
					v_tipoTarjeta,
					v_codigoVerificacion,
					v_FechaVencimiento
					FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.TarjetaBancaria" TARJBAN
						ON (PER."IdPersona" = TARJBAN."IdPersona")
					LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" BAN
						ON (TARJBAN."IdBanco" = BAN."IdBanco")
					LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoTarjetaBancaria" TIPTARJ
						ON (TARJBAN."IdTipoTarjetaBancaria" = TIPTARJ."IdTipoTarjetaBancaria")
					WHERE TARJBAN."IdTarjetaBancaria" = :p_IdTarjetaBancaria;
					
				IF v_FechaVencimiento = '0001-01-01' THEN 
					v_FechaVencimiento:='';
				END IF;


				INSERT INTO :v_DetalleLog VALUES(:V_subregistro, 'NumeroTarjetaBancaria', '', :v_numeroTarjetaBancaria);
							
				 IF v_nombreBanco <> v_vacio THEN 
					INSERT INTO :v_DetalleLog VALUES(:V_subregistro, 'Banco', '', :v_nombreBanco);
				END IF;
								
				 IF v_tipoTarjeta <> v_vacio THEN 
					INSERT INTO :v_DetalleLog VALUES(:V_subregistro, 'TipoTarjetaBancaria', '', :v_tipoTarjeta);
				 END IF;
								
				 IF v_codigoVerificacion <> 0 THEN
					INSERT INTO :v_DetalleLog VALUES(:V_subregistro, 'CodigoVerificacion', '', :v_codigoVerificacion);
				 END IF;
							
				 IF v_FechaVencimiento <> v_vacio AND v_FechaVencimiento <> '0001-01-01' THEN 
					INSERT INTO :v_DetalleLog VALUES(:V_subregistro, 'FechaVencimiento', '', :v_FechaVencimiento);
				 END IF;
				
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);

				CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
						p_IdTipoLog => 'INGRESO' 
						,p_IdModulo => 'SOC' 
						,p_IdUsuario => p_IdUsuario 
						,p_IdEmpresa => p_IdEmpresa 
						,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
						,p_IdUsuarioAtendido => p_IdUsuario
						,p_IdPersona => :v_idPersona
						,p_NombreCliente => :v_nombrePersona
						,p_IdDocumentoIdentidad => :v_documentoIdentidad
						,p_IdTipoDocumentoIdentidad => :v_tipoDocumentoIdentidad
						,p_IdTipoRegistro => v_TipoEntidad--'TARJETABANCARIA'
						,p_DetalleLog => :v_DetalleLog	-- hacia arriba obligatorios
						,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
						,p_IdRegistro => :v_IdEntidadOrigen
						,p_EstadoProceso => :p_IdEstadoAutorizacion
				);
			
			END;
			--FIN LOG
		END;

		--------------------
		--FIN SP 
		--------------------
	END;	
END;