SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarCrearPersonaCuentaBancaria";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a las cuentas de una persona.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 17-12-2015
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarCrearPersonaCuentaBancaria" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	
	
	IN 	p_IdCuentaBancaria								BIGINT,	
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
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'CUENTABANCARIA';
			--Declaracion variables Log
			DECLARE v_fechaHoraSistema 								NVARCHAR(40);
			DECLARE v_subregistro 									NVARCHAR(40):= 'CUENTABANCARIA';
			DECLARE v_NombreProceso									NVARCHAR(100):='Confirmar Ingreso cuenta Bancaria';
			DECLARE v_id_persona 									BIGINT;
			DECLARE v_nombre_persona 								NVARCHAR(200)='';
			DECLARE v_documento_identidad 							NVARCHAR(40)='';
			DECLARE v_tipo_documento_identidad 						NVARCHAR(40)='';
			DECLARE v_numeroCuentBancaria 							NVARCHAR(40):='';
			DECLARE v_nombreBanco									NVARCHAR(100):='';
			DECLARE v_Moneda										NVARCHAR(100):='';
			DECLARE v_tipoCuentaBanco								NVARCHAR(100):='';
			DECLARE v_plaza											NVARCHAR(100):='';
			DECLARE v_esPac											NVARCHAR(40):='';
		
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
			
			v_IdEntidadOrigen:=CAST (p_IdCuentaBancaria AS NVARCHAR(40));
			
			
			UPDATE "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
			SET "IdEstadoAutorizacion"=p_IdEstadoAutorizacion
			WHERE "IdCuentaBancaria"=p_IdCuentaBancaria;
			
			UPDATE "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
			SET "IdEstadoAutorizacion"=p_IdEstadoAutorizacion
			WHERE "IdCuentaBancaria"=p_IdCuentaBancaria;
	
			
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
					"p"."IdPersona" AS "IdPersona",
					CASE WHEN LENGTH("NOM"."NombreCompleto") > 100 THEN LEFT(("NOM"."NombreCompleto"),100) ELSE "NOM"."NombreCompleto" END AS "Nombres",
					"di"."DocumentoIdentidad" AS "DocumentoIdentidad",
					"di"."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad",
					COALESCE("cue"."NumeroCuentaBancaria", '') AS "NumeroCuentaBancaria",
					COALESCE("ban"."NombreBanco", '') AS "NombreBanco",
					COALESCE("mone"."Nombre", '') AS "NombreMoneda",
					COALESCE("ticue"."NombreTipoCuentaBancaria", '') AS "NombreTipoCuentaBancaria",
					COALESCE("cue"."EsPac", '') AS "EsPac" 
				INTO 
				v_id_persona,
				v_nombre_persona, 
				v_documento_identidad, 
				v_tipo_documento_identidad,
				v_numeroCuentBancaria,
				v_nombreBanco,
				v_Moneda,
				v_tipoCuentaBanco,
				v_esPac
					FROM "BSCL.SocioNegocio.BD::PER.Persona" "p"
					LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" "NOM" 
                        ON ("p"."IdPersona" = "NOM"."IdPersona")
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" "di" 
						ON ("p"."IdDocumentoIdentidad" = "di"."IdDocumentoIdentidad")
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.CuentaBancaria" "cue" 
						ON ("cue"."IdPersona" = "p"."IdPersona") 
					LEFT JOIN "BSCL.Plataforma.BD::BAN.Banco" "ban"
						ON ("ban"."IdBanco" = "cue"."IdBanco")
					LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" "mone"
						ON ("cue"."IdMoneda" = "mone"."IdMoneda")
					LEFT JOIN "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" "ticue"
						ON ("ticue"."IdTipoCuentaBancaria" = "cue"."IdTipoCuentaBancaria")
					WHERE "cue"."IdCuentaBancaria" = :p_IdCuentaBancaria;
						
				p_DetalleLog = (SELECT v_subregistro AS "SubRegistro", 'NumeroCuentaBancaria' AS "CampoRegistro", '' AS "ValorAntiguo", v_numeroCuentBancaria AS "ValorNuevo"
									FROM dummy
								UNION ALL 
								SELECT v_subregistro AS "SubRegistro", 'Banco' AS "CampoRegistro", '' AS "ValorAntiguo", v_nombreBanco AS "ValorNuevo"
									FROM dummy
								UNION ALL
								SELECT v_subregistro AS "SubRegistro", 'TipoCuentaBancaria' AS "CampoRegistro", '' AS "ValorAntiguo", v_tipoCuentaBanco AS "ValorNuevo"
									FROM dummy
								UNION ALL
								SELECT v_subregistro AS "SubRegistro", 'Moneda' AS "CampoRegistro", '' AS "ValorAntiguo", v_Moneda AS "ValorNuevo"
									FROM dummy
								UNION ALL 
								SELECT v_subregistro AS "SubRegistro", 'P.A.C' AS "CampoRegistro", '' AS "ValorAntiguo", v_esPac AS "ValorNuevo"
									FROM dummy);
				
						
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
						,p_IdPersona => :v_id_persona
						,p_NombreCliente => :v_nombre_persona
						,p_IdDocumentoIdentidad => :v_documento_identidad
						,p_IdTipoDocumentoIdentidad => :v_tipo_documento_identidad
						,p_IdTipoRegistro => v_TipoEntidad--'PERSONA'
						,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
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