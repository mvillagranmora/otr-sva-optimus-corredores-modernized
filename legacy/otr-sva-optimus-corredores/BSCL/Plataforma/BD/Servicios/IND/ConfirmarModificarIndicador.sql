SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::IND.ConfirmarModificarIndicador";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al indicador de una entidad.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 07-04-2016
--  
"BSCL.Plataforma.BD.Servicios::IND.ConfirmarModificarIndicador" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	
	
	
	IN 	p_IdSolicitudCambio							BIGINT,	
	IN  p_IdEstadoAutorizacion						NVARCHAR(3),
	IN  p_IdTipoEntidad			  					NVARCHAR(40),
	IN  p_IdEntidad				  					NVARCHAR(40),
	IN  p_ModeloCliente								NVARCHAR(40),
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
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
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
			DECLARE v_countDocId									BIGINT:=0;
			DECLARE v_countAutorizacionesPendientes					BIGINT;
			DECLARE v_IdEstadoAutorizacion_Calculado				NVARCHAR(3);
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_IdPortafolio									NVARCHAR(40):='';
			DECLARE v_IdTipoEntidad         						NVARCHAR(40):='';
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'INDICADOR';
			DECLARE v_CountIndicadorModificado						BIGINT:=0;
			DECLARE v_CountIndicadorNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_countFechaVencimiento							BIGINT:=0;
			DECLARE v_CountProposito								BIGINT:=0;
			DECLARE v_CodigoTipoIndicador							NVARCHAR(40):='';
			
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			
			SELECT COUNT(1) INTO v_countSolicitud
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
			WHERE "IdSolicitudCambio" =p_IdSolicitudCambio;
		
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
		--Logica Negocio
		--------------------
		BEGIN
				 
			--ACTUALIZACION DE VALORES (* ENTIDAD EXISTENTE *)
			--DATOS MAESTROS DE LA ENTIDAD 
			v_DatosSolicitudCambio = SELECT * 
									   FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" 
									  WHERE "IdSolicitudCambio" =p_IdSolicitudCambio;

			SELECT COUNT("IdSolicitudCambio") 
			  INTO v_CountIndicadorModificado 
			  FROM :v_DatosSolicitudCambio;
			
			IF (:v_CountIndicadorModificado > 0) THEN
				UPDATE  ATR
				SET
					ATR."ValorIndicador" = MOD."ValorNuevo",
					ATR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
					ATR."IdEstadoAutorizacion" =p_IdEstadoAutorizacion
				FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
						INNER JOIN :v_DatosSolicitudCambio MOD ON (ATR."IdIndicador"= cast (MOD."IdEntidadOrigen" as int))					
				WHERE 
						MOD."IdTipoEntidad"			= v_TipoEntidad
					AND MOD."IdSolicitudCambio" 	= p_IdSolicitudCambio
					AND ATR."IdIndicador"			= cast (MOD."IdEntidadOrigen" as int);

				--Si el indicador es relacionado a la Corredora (Articulo 100 o NCG380) se modifica tambien su valor a nivel de cliente
				IF(p_IdTipoEntidad = 'PERSONA') THEN
								
					v_ValorNuevo = SELECT
										ATR."IdEntidad" AS "IdEntidad",
										ATR."IdIndicador" AS "IdIndicador",
										ATR."IdTipoIndicador" AS "IdTipoIndicador",
										MOD."ValorNuevo" AS "ValorNuevo"
						FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
						INNER JOIN :v_DatosSolicitudCambio MOD
							ON (ATR."IdIndicador"= cast (MOD."IdEntidadOrigen" as int))			
						WHERE 
							MOD."IdTipoEntidad"			= v_TipoEntidad
						AND MOD."IdSolicitudCambio" 	= p_IdSolicitudCambio
						AND ATR."IdIndicador"			= cast (MOD."IdEntidadOrigen" as int);
								
					UPDATE ATR
					SET
						ATR."ValorIndicador" = D."IdDetalleIndicador",
						ATR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
						ATR."IdEstadoAutorizacion" =p_IdEstadoAutorizacion
					FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
					INNER JOIN ("BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresNormativosCliente" CIC
						INNER JOIN :v_ValorNuevo VAN 
							ON CIC."IdPersona" = VAN."IdEntidad" AND CIC."IdTipoIndicadorPersona" = VAN."IdTipoIndicador")
						ON ATR."IdIndicador" = CIC."IdIndicador"
					INNER JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" D ON D."IdTipoIndicador" = CIC."IdTipoIndicadorCliente"
					INNER JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" DIN ON TO_VARCHAR(DIN."IdDetalleIndicador") = TO_VARCHAR(VAN."ValorNuevo") AND D."Codigo" = DIN."Codigo"
					WHERE ATR."IdTipoEntidad" = 'CLIENTE'
					AND ATR."IdIndicador" = CIC."IdIndicador"
					AND CIC."CodigoTipoIndicadorCliente" = 'NCG380';
				
					UPDATE ATR
					SET
						ATR."ValorIndicador" = VAN."ValorNuevo",
						ATR."FechaModificacion" = CURRENT_UTCTIMESTAMP,
						ATR."IdEstadoAutorizacion" =p_IdEstadoAutorizacion
					FROM "BSCL.Plataforma.BD::IND.Indicador" ATR
					INNER JOIN ("BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresNormativosCliente" CIC
						INNER JOIN :v_ValorNuevo VAN 
							ON CIC."IdPersona" = VAN."IdEntidad" AND CIC."IdTipoIndicadorPersona" = VAN."IdTipoIndicador")
						ON ATR."IdIndicador" = CIC."IdIndicador"
					WHERE ATR."IdTipoEntidad" = 'CLIENTE'
					AND ATR."IdIndicador" = CIC."IdIndicador"
					AND CIC."CodigoTipoIndicadorCliente" = 'RELCORR';
					
				END IF;
				
				v_CampoModificado:='FechaVencimiento';
				SELECT count("ValorNuevo") INTO v_countFechaVencimiento
				FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
				AND "IdTipoEntidad"= v_TipoEntidad;
				
				IF (v_countFechaVencimiento > 0) THEN
					UPDATE  DOC
					SET
						DOC."FechaVencimiento" = MOD."ValorNuevo",
						DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Plataforma.BD::IND.Indicador" DOC
						INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdIndicador"= cast (MOD."IdEntidadOrigen" as int))					
					WHERE 
						MOD."IdTipoEntidad"			= v_TipoEntidad
						AND MOD."IdSolicitudCambio" = p_IdSolicitudCambio
						AND DOC."IdIndicador"		= cast (MOD."IdEntidadOrigen" as int);
				END IF;
				
				/* APV REPLICA*/
				
				SELECT TOP 1 "IdEntidadOrigen" 
				  INTO v_IdEntidadOrigen 
				  FROM :v_DatosSolicitudCambio;
				
				SELECT "IdEntidad", "IdTipoEntidad" 
				  INTO v_IdPortafolio, v_IdTipoEntidad 
				  FROM "BSCL.Plataforma.BD::IND.Indicador"
				 WHERE "IdIndicador" = v_IdEntidadOrigen;

				IF(:v_IdTipoEntidad = 'PORTAFOLIO') THEN
				 SELECT COUNT(Portafolio."IdPortafolio") 
				   INTO v_CountProposito
				   FROM "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
				  WHERE Portafolio."Proposito" = 'PROPOSITOAPV'
					AND Portafolio."IdPortafolio" = v_IdPortafolio;	
				END IF;

				IF(:v_CountProposito > 0) THEN						
					CALL "BSCL.APV.BD.Servicios::ReplicaPortafolioAPV" (
							:p_IdEmpresa,
							:p_IdUsuario,
							:p_IdModulo,
							:p_UsaReglasNegocio,
							v_IdPortafolio, 
							:p_IdSolicitudCambio,
							'INDICADORES', 
							'U',
							p_out_AdvertenciasContador,
							p_out_AdvertenciasDetalle,
							p_out_ErroresContador,
							p_out_ErroresDetalle
						);
				 END IF;
				 
				 /* FIN APV */
			END IF;	
			--Se termina el flujo con la actualizacion del registro de solicitud.
			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambio" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_IdSolicitudCambio		=>:p_IdSolicitudCambio,
				p_IdEstadoAutorizacion	=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador			=>:p_out_ErroresContador,
				p_out_ErroresDetalle			=>:p_out_ErroresDetalle
			);

		END;
				--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_DetalleLog 						TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(500), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE	v_TipoEntidadBase 					NVARCHAR(40);
				DECLARE	v_IdEntidadBase						BIGINT;
				DECLARE v_fechaHoraSistema 					NVARCHAR(40);
				DECLARE v_NombreProceso						NVARCHAR(100):='Confirmar Cambio valor Indicador';
				DECLARE v_IdCliente							BIGINT:=0;
				DECLARE v_EstadoCliente						NVARCHAR(5):='';
				DECLARE	v_NombreCliente						NVARCHAR(100):='';
				DECLARE	v_IdDocumentoIdentidad				NVARCHAR(40):='';
				DECLARE	v_IdTipoDocumentoIdentidad			NVARCHAR(40):='';
				DECLARE v_IdPortafolio						BIGINT:=0;
				DECLARE v_EstadoPortafolio					NVARCHAR(5):='';
				
				SELECT TOP 1 "IdEntidadOrigen" 
				  INTO v_IdEntidadOrigen 
				  FROM :v_DatosSolicitudCambio;
				
				SELECT 	TOP 1 
						Indicador."IdTipoEntidad",
						Indicador."IdEntidadOrigen"
				  INTO v_TipoEntidadBase,
					   v_IdEntidadBase
				  FROM :v_DatosSolicitudCambio AS Indicador;
				 
				 --CONTINUO CONTRUYENDO DATOS CABECERA LOG
				 BEGIN 
					DECLARE v_TempTipoEntidadIndicador					NVARCHAR(40):='';
					DECLARE v_IdEntidadBaseTemporal						BIGINT:=0;
					DECLARE v_IdPersonaEmpleado							BIGINT:=0;
					DECLARE v_IdPersonaCliente							BIGINT:=0;
					DECLARE v_IdClientePortafolio						BIGINT:=0;
					DECLARE v_IdPersonaClientePortafolio				BIGINT:=0;
					
					 IF(v_TipoEntidadBase = 'INDICADOR') THEN
						
						v_IdEntidadBase:= COALESCE(v_IdEntidadBase, 0);
						
						SELECT
							COALESCE("IdTipoEntidad", ''),
							COALESCE("IdEntidad", 0)
						INTO
							v_TempTipoEntidadIndicador,
							v_IdEntidadBaseTemporal
								FROM "BSCL.Plataforma.BD::IND.Indicador"
								WHERE "IdIndicador" = :v_IdEntidadBase;

						IF(v_TempTipoEntidadIndicador = 'PERSONA')THEN
						
							v_TipoEntidadBase:=v_TipoEntidadBase||v_TempTipoEntidadIndicador;
							
							SELECT
								CASE
									COALESCE("PER"."Tipo", '') WHEN 'N'
									THEN COALESCE("PER"."Nombres", '') || ' ' || COALESCE("PER"."ApellidoPaterno", '') || ' ' || COALESCE("PER"."ApellidoMaterno", '')
									ELSE COALESCE("PER"."RazonSocial" ,'')
								END AS "NombreCompleto"
							INTO
								v_NombreCliente
							FROM "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
							WHERE "PER"."IdPersona" = :v_IdEntidadBaseTemporal;
							
							SELECT
								COALESCE("DOC"."DocumentoIdentidad", ''),
								COALESCE("DOC"."IdTipoDocumentoIdentidad", '')
							INTO
								v_IdDocumentoIdentidad,
								v_IdTipoDocumentoIdentidad
							FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
							WHERE "DOC"."IdPersona" = :v_IdEntidadBaseTemporal
							AND DOC."Predeterminado" = 'S';
						
						END IF;
						
						IF(v_TempTipoEntidadIndicador = 'CLIENTE') THEN
						
							v_TipoEntidadBase:=v_TipoEntidadBase||v_TempTipoEntidadIndicador;
							
							IF(p_ModeloCliente <>'' AND p_ModeloCliente <> '0') THEN
						    	v_IdCliente = 0;
								v_IdPersonaCliente = 0;
								v_EstadoCliente = '';
								v_NombreCliente = '';
								v_IdDocumentoIdentidad = '';
								v_IdTipoDocumentoIdentidad = '';
							ELSE
							
							SELECT
								COALESCE("CLI"."IdCliente", 0),
								COALESCE("CLI"."IdPersona", 0),
								COALESCE("CLI"."IdEstado", '')
							INTO
								v_IdCliente,
								v_IdPersonaCliente,
								v_EstadoCliente
							FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"
							WHERE "CLI"."IdCliente" = :v_IdEntidadBaseTemporal;
							
							SELECT
								CASE
									COALESCE("PER"."Tipo", '') WHEN 'N'
									THEN COALESCE("PER"."Nombres", '') || ' ' || COALESCE("PER"."ApellidoPaterno", '') || ' ' || COALESCE("PER"."ApellidoMaterno", '')
									ELSE COALESCE("PER"."RazonSocial" ,'')
								END AS "NombreCompleto"
							INTO
								v_NombreCliente
							FROM "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
							WHERE "PER"."IdPersona" = :v_IdPersonaCliente;
							
							
							SELECT COUNT(1) INTO v_countDocId
							FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
							WHERE "DOC"."IdPersona" = :v_IdPersonaCliente
							AND DOC."Predeterminado" = 'S';
							
							IF (v_countDocId = 0) THEN
									p_out_ErroresDetalle =  select 'DOCUMENTO_IDENTIDAD_INVALIDO' as "Codigo",'Cliente sin documento identidad predeterminado' as "Descripcion" from dummy;
									SIGNAL SALIDA_CONTROLADA; 	
		     				END IF;
							
							SELECT 
								COALESCE("DOC"."DocumentoIdentidad", ''),
								COALESCE("DOC"."IdTipoDocumentoIdentidad", '')
							INTO 	
								v_IdDocumentoIdentidad,
								v_IdTipoDocumentoIdentidad
							FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
							WHERE "DOC"."IdPersona" = :v_IdPersonaCliente
							AND DOC."Predeterminado" = 'S';
							
							END IF;
							
						END IF;
							
						IF(v_TempTipoEntidadIndicador = 'EMPLEADO')THEN
							
							v_TipoEntidadBase:=v_TipoEntidadBase||v_TempTipoEntidadIndicador;
							
							SELECT
								COALESCE("EMP"."IdPersona", 0)
							INTO
								v_IdPersonaEmpleado
							FROM "BSCL.SocioNegocio.BD::EMP.Empleado" AS "EMP"
							WHERE "EMP"."IdEmpleado" = :v_IdEntidadBaseTemporal;
						
							SELECT
								CASE
									COALESCE("PER"."Tipo", '') WHEN 'N'
									THEN COALESCE("PER"."Nombres", '') || ' ' || COALESCE("PER"."ApellidoPaterno", '') || ' ' || COALESCE("PER"."ApellidoMaterno", '')
									ELSE COALESCE("PER"."RazonSocial" ,'')
								END AS "NombreCompleto"
							INTO 
								v_NombreCliente
							FROM "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
							WHERE "PER"."IdPersona" = :v_IdPersonaEmpleado;
							
							SELECT 
								COALESCE("DOC"."DocumentoIdentidad", ''),
								COALESCE("DOC"."IdTipoDocumentoIdentidad", '')
							INTO 	
								v_IdDocumentoIdentidad,
								v_IdTipoDocumentoIdentidad
							FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
							WHERE "DOC"."IdPersona" = :v_IdPersonaEmpleado
							AND DOC."Predeterminado" = 'S';
								
						END IF;
						
						IF(v_TempTipoEntidadIndicador = 'PORTAFOLIO')THEN
							
							v_TipoEntidadBase:=v_TipoEntidadBase||v_TempTipoEntidadIndicador;
							
							IF(p_ModeloCliente <>'' AND p_ModeloCliente <> '0') THEN
								v_IdClientePortafolio = 0;
								v_IdPortafolio = 0;
								v_EstadoPortafolio = '';
								v_IdCliente = 0;
								v_IdPersonaClientePortafolio = 0;
								v_EstadoCliente = '';
								v_IdDocumentoIdentidad = 0;
								v_IdTipoDocumentoIdentidad = 0;
								v_NombreCliente = '';
							ELSE
							
							SELECT
								COALESCE("PORT"."IdCliente", 0),
								COALESCE("PORT"."IdPortafolio", 0),
								COALESCE("PORT"."IdEstado", '')
							INTO
								v_IdClientePortafolio,
								v_IdPortafolio,
								v_EstadoPortafolio
							FROM "BSCL.Portafolio.BD::POR.Portafolio" AS "PORT"
							WHERE "PORT"."IdPortafolio" = :v_IdEntidadBaseTemporal;
							
							SELECT
								COALESCE("CLI"."IdCliente", 0),
								COALESCE("CLI"."IdPersona", 0),
								COALESCE("CLI"."IdEstado", '')
							INTO
								v_IdCliente,
								v_IdPersonaClientePortafolio,
								v_EstadoCliente
							FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"
							WHERE "CLI"."IdCliente" = :v_IdClientePortafolio;
							
							SELECT
								CASE
									COALESCE("PER"."Tipo", '') WHEN 'N'
									THEN COALESCE("PER"."Nombres", '') || ' ' || COALESCE("PER"."ApellidoPaterno", '') || ' ' || COALESCE("PER"."ApellidoMaterno", '')
									ELSE COALESCE("PER"."RazonSocial" ,'')
								END AS "NombreCompleto"
							INTO
								v_NombreCliente
							FROM "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
							WHERE "PER"."IdPersona" = :v_IdPersonaClientePortafolio;
							
							SELECT
								COALESCE("DOC"."DocumentoIdentidad", ''),
								COALESCE("DOC"."IdTipoDocumentoIdentidad", '')
							INTO
								v_IdDocumentoIdentidad,
								v_IdTipoDocumentoIdentidad
							FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC"
							WHERE "DOC"."IdPersona" = :v_IdPersonaClientePortafolio
							AND DOC."Predeterminado" = 'S';
							
							END IF;
							
						END IF;
								
					END IF;
				
					--PREPAREMOS DETALLE LOG
					IF(v_TempTipoEntidadIndicador = 'CLIENTE' OR v_TempTipoEntidadIndicador = 'PORTAFOLIO') THEN 
						
						v_DetalleLog = (SELECT 
											MOD."IdTipoEntidad"		 								AS "SubRegistro",
											CAST("TIPIND"."NombreTipoIndicador" AS NVARCHAR(255)) 	AS "CampoRegistro",
											CASE 
												WHEN TRIM(MOD."ValorAntiguo") = '0001-01-01 00:00:00' THEN ''
												ELSE MOD."ValorAntiguo"  
											END 													AS "ValorAntiguo",
											MOD."ValorNuevo" 									
												FROM :v_DatosSolicitudCambio MOD
												LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" 						AS "IND"
													ON (MOD."IdEntidadOrigen" = "IND"."IdIndicador")
												INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" 					AS 	"TIPIND"
													ON("IND"."IdTipoIndicador" = "TIPIND"."IdTipoIndicador" AND "IND"."IdTipoEntidad" = "TIPIND"."IdTipoEntidad"
													AND ("TIPIND"."ClaseDato" = 'CHECKBOX' OR "TIPIND"."ClaseDato" = 'NVARCHAR' OR "TIPIND"."ClaseDato" = 'DATE'))
												WHERE MOD."IdSolicitudCambio" = :p_IdSolicitudCambio
												AND ((REPLACE(MOD."ValorAntiguo", '0001-01-01 00:00:00', 'Default') <> 'Default' OR REPLACE(MOD."ValorNuevo", '0001-01-01 00:00:00', 'Default') <> 'Default')
												AND MOD."ValorAntiguo" <>  MOD."ValorNuevo")	
										);
							
						v_DetalleLog = (SELECT *
											FROM :v_DetalleLog
										UNION ALL 
										SELECT 
											MOD."IdTipoEntidad"		 								AS "SubRegistro",
											CAST("TIPIND"."NombreTipoIndicador"AS NVARCHAR(255)) 	AS "CampoRegistro", 
											CASE 
												WHEN TRIM(MOD."ValorAntiguo") = '0001-01-01 00:00:00' THEN ''
												WHEN COALESCE(MOD."ValorAntiguo", '') = CAST(COALESCE("DETAINDIANT"."IdDetalleIndicador", 0) AS NVARCHAR(40)) THEN "DETAINDIANT"."DetalleIndicador" 
												ELSE MOD."ValorAntiguo"
											END  													AS "ValorAntiguo", 
											CASE
												WHEN COALESCE(MOD."ValorNuevo", '') = CAST(COALESCE("DETAINDINUE"."IdDetalleIndicador", 0) AS NVARCHAR(40)) THEN "DETAINDINUE"."DetalleIndicador"  
												ELSE MOD."ValorNuevo"
											END 													AS "ValorNuevo" 
												FROM :v_DatosSolicitudCambio MOD
												LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" "IND"
													ON (MOD."IdEntidadOrigen" = "IND"."IdIndicador")
												INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" "TIPIND"
													ON("IND"."IdTipoIndicador" = "TIPIND"."IdTipoIndicador"	AND "IND"."IdTipoEntidad" = "TIPIND"."IdTipoEntidad" 
														AND "TIPIND"."ClaseDato" = 'COMBO')
												LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" "DETAINDIANT"
													ON(COALESCE(MOD."CampoModificado", '0') = CAST("DETAINDIANT"."IdTipoIndicador" AS NVARCHAR(40))
													AND COALESCE(MOD."ValorAntiguo", '0') = CAST("DETAINDIANT"."IdDetalleIndicador" AS NVARCHAR(40)))
												LEFT JOIN "BSCL.Plataforma.BD::IND.DetalleIndicador" "DETAINDINUE"
													ON(COALESCE(MOD."CampoModificado", '0') = CAST("DETAINDINUE"."IdTipoIndicador" AS NVARCHAR(40))
													AND COALESCE(MOD."ValorNuevo", '0') = CAST("DETAINDINUE"."IdDetalleIndicador" AS NVARCHAR(40)))
												WHERE MOD."IdSolicitudCambio" = :p_IdSolicitudCambio
												AND ((REPLACE(MOD."ValorAntiguo", '0001-01-01 00:00:00', 'Default') <> 'Default' OR REPLACE(MOD."ValorNuevo", '0001-01-01 00:00:00', 'Default') <> 'Default') 
												AND MOD."ValorAntiguo" <>  MOD."ValorNuevo")
										);
					ELSE
						v_DetalleLog = (SELECT 	
											MOD."IdTipoEntidad"		 		as "SubRegistro",
											TIPIND."NombreTipoIndicador" 	as "CampoRegistro",
											MOD."ValorAntiguo"  	 		as "ValorAntiguo",
											MOD."ValorNuevo" 		 		as "ValorNuevo" 
												FROM :v_DatosSolicitudCambio MOD
												JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TIPIND ON (TIPIND."IdTipoIndicador" = MOD."CampoModificado")
								);
					END IF;
				END;

				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => v_TipoEntidadBase--
					,p_DetalleLog => :v_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => TO_VARCHAR(:v_IdEntidadBase)
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					,p_EstadoRegistro=> :p_IdEstadoAutorizacion
					,p_IdPortafolio => :v_IdPortafolio
					,p_EstadoPortafolio => :v_EstadoPortafolio
					,p_IdCliente => :v_IdCliente
					,p_NombreCliente => :v_NombreCliente
					,p_IdTipoDocumentoIdentidad => :v_IdTipoDocumentoIdentidad
					,p_IdDocumentoIdentidad => :v_IdDocumentoIdentidad
					,p_EstadoCliente => :v_EstadoCliente						
				);
			END;
			--LOG DE AUDITORIA (Fin)			
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;