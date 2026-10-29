SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaTelefono";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a los telefonos de una persona.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 17-12-2015
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarModificarPersonaTelefono" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN 	p_IdTelefono								BIGINT,	
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
			DECLARE v_countSolicitud								BIGINT:=0;
			DECLARE v_countAutorizacionesPendientes					BIGINT;
			DECLARE v_IdEntidadOrigen								NVARCHAR(40);
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'TELEFONO';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_IdPais			 						BIGINT;
			DECLARE v_count_IdTipoTelefono	 						BIGINT;
			DECLARE v_count_Numero 									BIGINT;
			DECLARE v_count_Anexo 									BIGINT;
			DECLARE v_count_CodigoProposito							BIGINT;
			DECLARE v_count_Comentario								BIGINT;
			DECLARE v_count_IdEstado								BIGINT;
			
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdTelefono AS NVARCHAR(40));
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdEntidadOrigen"  	= v_IdEntidadOrigen
			 AND "IdTipoEntidad" 		= v_TipoEntidad
  			 AND "IdEstadoAutorizacion"	='PEN';
		
			IF (v_countSolicitud = 0) THEN
				p_out_ErroresDetalle =  select 'SOLICITUD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				--p_out_ErroresDetalle =  select cast(v_countSolicitud as nvarchar(40)) as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	     	END IF;

			
		END;

		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			

			--ACTUALIZACION DE VALORES (* ENTIDAD EXISTENTE *)
									
			--DATOS MAESTROS DE LA ENTIDAD 
			v_DatosSolicitudCambio = SELECT * FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdEntidadOrigen"  	= v_IdEntidadOrigen
			 AND "IdTipoEntidad" 		= v_TipoEntidad
  			 AND "IdEstadoAutorizacion"	='PEN';
			
			SELECT 	COUNT("IdSolicitudCambio") INTO	v_CountAtributoModificado FROM	:v_DatosSolicitudCambio;
			
			IF (v_CountAtributoModificado > 0) 	 THEN
			
			
			
					v_CampoModificado:='IdPais';
					SELECT count("ValorNuevo") INTO v_count_IdPais
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdPais > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPais" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdTelefono"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdTelefono"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdTipoTelefono';
					SELECT count("ValorNuevo") INTO v_count_IdTipoTelefono
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdTipoTelefono > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdTipoTelefono" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdTelefono"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdTelefono"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='Numero';
					SELECT count("ValorNuevo") INTO v_count_Numero
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Numero > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."Numero" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdTelefono"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdTelefono"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='Anexo';
					SELECT count("ValorNuevo") INTO v_count_Anexo
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Anexo > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."Anexo" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdTelefono"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdTelefono"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;										
										
					v_CampoModificado:='CodigoProposito';
					SELECT count("ValorNuevo") INTO v_count_CodigoProposito
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_CodigoProposito > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."CodigoProposito" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdTelefono"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdTelefono"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;										

					v_CampoModificado:='Comentario';
					SELECT count("ValorNuevo") INTO v_count_Comentario
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Comentario > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."Comentario" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdTelefono"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdTelefono"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;										
																	
					v_CampoModificado:='IdEstado';
					SELECT count("ValorNuevo") INTO v_count_IdEstado
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_IdEstado > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdEstado" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.SocioNegocio.BD::PER.Telefono" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdTelefono"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdTelefono"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;										
	
			END IF;	
			--Se termina el flujo con la actualizacion del registro de solicitud.

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
						
		END;


			--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Solicitud de Cambio';
				DECLARE v_id_persona BIGINT;
				DECLARE v_nombre_persona NVARCHAR(200)='';
				DECLARE v_documento_identidad NVARCHAR(40)='';
				DECLARE v_tipo_documento_identidad NVARCHAR(40)='';
				DECLARE v_id_cliente BIGINT DEFAULT 0;
				DECLARE v_count_idcliente BIGINT DEFAULT 0;
				
				--DATOS CABECERA LOG
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				p_DetalleLog = 
				(Select MOD."IdTipoEntidad"		 as "SubRegistro",
						MOD."CampoModificado"	 as "CampoRegistro",
						CASE 
							WHEN MOD."CampoModificado" = 'CodigoProposito' AND "PROCONTANT"."IdPropositoMedioContacto" = COALESCE(MOD."ValorAntiguo", '') THEN "PROCONTANT"."Descripcion" 
							WHEN MOD."CampoModificado" = 'IdPais' AND CAST("PAISANT"."IdPais" AS NVARCHAR) =  COALESCE(MOD."ValorAntiguo", '') THEN "PAISANT"."NombrePais" 
							WHEN MOD."CampoModificado" = 'IdEstado' AND "ESTANT"."IdEstado" = COALESCE(MOD."ValorAntiguo", '') THEN "ESTANT"."Nombre" 
							ELSE MOD."ValorAntiguo"
						END AS "ValorAntiguo",
						CASE 
							WHEN MOD."CampoModificado" = 'CodigoProposito' AND "PROCONTNUE"."IdPropositoMedioContacto" = COALESCE(MOD."ValorNuevo", '') THEN "PROCONTNUE"."Descripcion" 
							WHEN MOD."CampoModificado" = 'IdPais' AND CAST("PAISNUE"."IdPais" AS NVARCHAR) =  COALESCE(MOD."ValorNuevo", '') THEN "PAISNUE"."NombrePais" 
							WHEN MOD."CampoModificado" = 'IdEstado' AND "ESTNUE"."IdEstado" = COALESCE(MOD."ValorNuevo", '') THEN "ESTNUE"."Nombre" 
							ELSE MOD."ValorNuevo"
						END AS "ValorNuevo"
							FROM :v_DatosSolicitudCambio MOD
							LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" 	AS "PROCONTANT" 	ON("PROCONTANT"."IdPropositoMedioContacto" = COALESCE(MOD."ValorAntiguo", ''))
							LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" 	AS "PROCONTNUE" 	ON("PROCONTNUE"."IdPropositoMedioContacto" = COALESCE(MOD."ValorNuevo", ''))
							LEFT JOIN "BSCL.Plataforma.BD::REG.Pais"						AS "PAISANT"		ON(CAST("PAISANT"."IdPais" AS NVARCHAR) = COALESCE(MOD."ValorAntiguo", ''))
							LEFT JOIN "BSCL.Plataforma.BD::REG.Pais"						AS "PAISNUE"		ON(CAST("PAISNUE"."IdPais" AS NVARCHAR) = COALESCE(MOD."ValorNuevo", ''))
							LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado"						AS "ESTANT"			ON("ESTANT"."IdEstado" = COALESCE(MOD."ValorAntiguo", ''))
							LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado"						AS "ESTNUE"			ON("ESTNUE"."IdEstado" = COALESCE(MOD."ValorNuevo", ''))
				);
				
				--GRABAR LOG
					SELECT "IdPersona" INTO v_id_persona FROM "BSCL.SocioNegocio.BD::PER.Telefono"  WHERE "IdTelefono" = :p_IdTelefono;
					SELECT COUNT(1) INTO v_count_idcliente FROM "BSCL.SocioNegocio.BD::CLI.Cliente" WHERE "IdPersona" = v_id_persona;
					
					IF (v_count_idcliente > 0) THEN
						SELECT "IdCliente" INTO v_id_cliente FROM "BSCL.SocioNegocio.BD::CLI.Cliente" WHERE "IdPersona" = v_id_persona;
					END IF;
					
				SELECT 
					CASE WHEN LENGTH("NOM"."NombreCompleto") > 100 THEN LEFT(("NOM"."NombreCompleto"),100) ELSE "NOM"."NombreCompleto" END AS "Nombres",
					"di"."DocumentoIdentidad" AS "DocumentoIdentidad",
					"di"."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidad"
					INTO v_nombre_persona, v_documento_identidad, v_tipo_documento_identidad
						FROM "BSCL.SocioNegocio.BD::PER.Persona" "p"
						LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" "NOM" ON ("p"."IdPersona" = "NOM"."IdPersona")
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" "di" ON ("p"."IdDocumentoIdentidad" = "di"."IdDocumentoIdentidad")
							WHERE "p"."IdPersona" = v_id_persona;
					
					CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdPersona => :v_id_persona
					,p_IdCliente => :v_id_cliente
					,p_NombreCliente => :v_nombre_persona
					,p_IdDocumentoIdentidad => :v_documento_identidad
					,p_IdTipoDocumentoIdentidad => :v_tipo_documento_identidad
					,p_IdTipoRegistro => v_TipoEntidad--'CLIENTE'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadOrigen
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					);
			END;
			--LOG DE AUDITORIA (Fin)			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;