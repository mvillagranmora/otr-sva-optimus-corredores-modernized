SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarPredeterminarPersonaCuentaBancaria";

CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la predeterminacion de las Cuentas Bancarias de una persona.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: HBRAVO
--  Fecha Creacion: 08-07-2012
--  
"BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarPredeterminarPersonaCuentaBancaria" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN 	p_CuentaBancaria							BIGINT,	
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
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'CUENTABANCARIA-PREDETERMINAR';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			DECLARE v_id_moneda                    					NVARCHAR(40)='';
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_Calle			 						BIGINT;
			DECLARE v_count_Numero 									BIGINT;
			DECLARE v_count_Departamento 							BIGINT;
			DECLARE v_count_Sector									BIGINT;
			DECLARE v_count_CodigoPostal							BIGINT;
			DECLARE v_count_IdCiudad								BIGINT;
			DECLARE v_count_IdDivisionPais							BIGINT;
			DECLARE v_count_CodigoProposito							BIGINT;
			DECLARE v_count_Comentario								BIGINT;
			DECLARE v_count_IdEstado								BIGINT;
			--OBTENER DIRECCION ANTIGUO
			DECLARE v_CuentaAnterior								NVARCHAR(40):='';	
			--VARIABLES LOGS
			DECLARE v_fechaHoraSistema 								NVARCHAR(40);
			DECLARE v_NombreProceso									NVARCHAR(100):='Confirmar Predeterminar Cuenta Bancaria';
			DECLARE v_subregistro 									NVARCHAR(40):= 'CUENTABANCARIA';
			DECLARE v_id_persona 									BIGINT;
			DECLARE v_nombrePersona 								NVARCHAR(200)='';
			DECLARE v_documentoIdentidad 							NVARCHAR(40)='';
			DECLARE v_tipoDocumentoIdentidad 						NVARCHAR(40)='';
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	
		BEGIN
			v_IdEntidadOrigen:=CAST (p_CuentaBancaria AS NVARCHAR(40));
			
			SELECT COUNT("IdSolicitudCambio") INTO v_countSolicitud
            FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"
            WHERE "IdEntidadOrigen"  	= v_IdEntidadOrigen
			 AND "IdTipoEntidad" 		= v_TipoEntidad
  			 AND "IdEstadoAutorizacion"	='PEN';
                
                IF(v_countSolicitud=0) THEN
                p_out_ErroresDetalle =  select 'SOLICITUD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
			DECLARE v_IdPersona BIGINT;
			DECLARE v_IdCuentaBancaria BIGINT;

			--ACTUALIZACION DE VALORES (* ENTIDAD EXISTENTE *)
									
			--DATOS MAESTROS DE LA ENTIDAD 
			v_DatosSolicitudCambio = SELECT * 
            FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"
            WHERE "IdEntidadOrigen"  	= v_IdEntidadOrigen
			 AND "IdTipoEntidad" 		= v_TipoEntidad
  			 AND "IdEstadoAutorizacion"	='PEN';
            
            SELECT  COUNT("IdSolicitudCambio") INTO v_CountAtributoModificado FROM  :v_DatosSolicitudCambio;
            
			IF (v_CountAtributoModificado > 0) 	 THEN
			
			
					v_CampoModificado:='Predeterminado';
					SELECT count("ValorNuevo") INTO v_count_Calle
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Calle > 0) THEN
					
						--obtener el idTelefono y idPersona a modificar
						v_persona_cuentabancaria = SELECT "IdPersona", cast(MOD."IdEntidadOrigen" AS BIGINT) AS "IdCuentaBancaria"
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CTB
						INNER JOIN :v_DatosSolicitudCambio MOD ON (CTB."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
							MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND CTB."IdCuentaBancaria"	= cast (MOD."IdEntidadOrigen" as int);
							
						--quitar como predeterminado la Cuenta Bancaria anterior de esa moneda
						SELECT "IdPersona", "IdMoneda" INTO v_id_persona, v_id_moneda FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" WHERE "IdCuentaBancaria" = :p_CuentaBancaria;
						UPDATE "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
						SET "Predeterminado" = 'N'
						WHERE "IdPersona" = v_id_persona AND "IdMoneda" = v_id_moneda;
		
						--asignar como predeterminado la Cuentas Bancarias
						UPDATE  CTB
						SET
							CTB."Predeterminado" = MOD."ValorNuevo",
							CTB."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" CTB
							INNER JOIN :v_DatosSolicitudCambio MOD ON (CTB."IdCuentaBancaria"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
							MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND CTB."IdCuentaBancaria"	= cast (MOD."IdEntidadOrigen" as int);
							v_countProcesados:=v_countProcesados + 1;
						
						/*RECUPERANDO TELEFONO PRINCIPAL ANTES DEL UPDATE*/
			           	SELECT "IdPersona" INTO v_IdPersona FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"  WHERE "IdCuentaBancaria" = :p_CuentaBancaria;

			           	SELECT COALESCE("IdCuentaBancariaPrincipal", 0) INTO v_IdCuentaBancaria FROM "BSCL.SocioNegocio.BD::PER.Persona" WHERE "IdPersona" = :v_IdPersona;

			           	IF (v_IdCuentaBancaria <> 0) THEN 
			           		SELECT 
			           			COALESCE(DIREC."NumeroCuentaBancaria", '') AS "NumeroCuentaBancaria"
			           			INTO 
			           			v_CuentaAnterior
			           				FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" DIREC
			          					WHERE DIREC."IdCuentaBancaria" = :v_IdCuentaBancaria;
			          				
			           	END IF;
			            /*FIN RECUPERAR TELEFONO ANTERIOR*/
							
						--finalmente actualizar el telefono principal de la persona
						UPDATE P
						SET P."IdCuentaBancariaPrincipal" = PDE."IdCuentaBancaria"
						FROM "BSCL.SocioNegocio.BD::PER.Persona" P, :v_persona_cuentabancaria PDE
						WHERE P."IdPersona" = PDE."IdPersona";

						/* ACTUALIZAR ESTADO PREFERENCIAS DE PAGO */
						UPDATE "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
						SET "IdEstadoAutorizacion"= p_IdEstadoAutorizacion
						WHERE "IdCuentaBancaria"= p_CuentaBancaria;

					END IF;

					
			END IF;	
			--Se termina el flujo con la actualizacion del registro de solicitud.
            v_SolicitudCambio = SELECT 0 as "IdSolicitudCambio" FROM dummy where 1=2;
            v_EntidadOrigen = SELECT "IdEntidadOrigen", "IdTipoEntidad" FROM :v_DatosSolicitudCambio;
            CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo" (
                p_IdEmpresa             =>:p_IdEmpresa,
                p_IdUsuario             =>:p_IdUsuario,
                p_IdModulo              =>:p_IdModulo,
                p_SolicitudCambio           =>:v_SolicitudCambio,
                p_EntidadOrigen             =>:v_EntidadOrigen,
                p_IdEstadoAutorizacion  =>:p_IdEstadoAutorizacion,
                p_out_AdvertenciasContador      =>:p_out_AdvertenciasContador,
                p_out_AdvertenciasDetalle       =>:p_out_AdvertenciasDetalle,   
                p_out_ErroresContador           =>:p_out_ErroresContador,
                p_out_ErroresDetalle            =>:p_out_ErroresDetalle 
            );
						
		END;	
			--LOG DE AUDITORIA (inicio)
			BEGIN 
				
				--GRABAR LOG
				SELECT "IdPersona" INTO v_id_persona FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" WHERE "IdCuentaBancaria" = :p_CuentaBancaria;
				
				SELECT  
					CASE WHEN LENGTH(NOM."NombreCompleto") > 100 THEN LEFT((NOM."NombreCompleto"),100) ELSE NOM."NombreCompleto" END AS "Nombres"
					,DI."DocumentoIdentidad"
					,DI."IdTipoDocumentoIdentidad" 
				INTO 
					v_nombrePersona
					,v_documentoIdentidad
					,v_tipoDocumentoIdentidad
						FROM "BSCL.SocioNegocio.BD::PER.Persona" PER
						LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI 
							ON (PER."IdDocumentoIdentidad" = DI."IdDocumentoIdentidad")
						LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" NOM 
                            ON (PER."IdPersona" = NOM."IdPersona")
						WHERE PER."IdPersona" = :v_id_persona;
					
				p_DetalleLog = (SELECT 
									v_subregistro				 					AS "SubRegistro"
									,'Predeterminado' 								AS "CampoRegistro"
									,v_CuentaAnterior								AS "ValorAntiguo"
									,COALESCE("NumeroCuentaBancaria", '') 			AS "ValorNuevo"
										FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
										WHERE "IdCuentaBancaria" = :p_CuentaBancaria);
									
				--DATOS CABECERA LOG
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
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
					,p_IdPersona => :v_id_persona
					,p_NombreCliente => :v_nombrePersona
					,p_IdDocumentoIdentidad => :v_documentoIdentidad
					,p_IdTipoDocumentoIdentidad => :v_tipoDocumentoIdentidad
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