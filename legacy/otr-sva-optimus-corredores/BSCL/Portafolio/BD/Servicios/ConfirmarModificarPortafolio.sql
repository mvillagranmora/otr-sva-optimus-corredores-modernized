SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Portafolio.BD.Servicios::ConfirmarModificarPortafolio";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion al portafolio de un cliente.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 21-03-2016
--  Fecha Modificacion: 04-04-2016  (deja de ser _new y pasa a ser el sp que se utilizara)
--  
"BSCL.Portafolio.BD.Servicios::ConfirmarModificarPortafolio" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico

	IN 	p_IdPortafolio								BIGINT,	
	IN  p_IdEstadoAutorizacion						NVARCHAR(3),
	IN  p_IdCanal									NVARCHAR(3) ,
	
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
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'PORTAFOLIO';
			DECLARE v_CampoModificado								NVARCHAR(255):='';
			DECLARE v_CountAtributoModificado						BIGINT:=0;
			DECLARE v_CountAtributoNuevo							BIGINT:=0;
			DECLARE v_countProcesados								BIGINT:=0;
			DECLARE v_IdCliente										BIGINT;
			DECLARE v_IdCategoriaInversionista						NVARCHAR(40);
		    DECLARE v_IdPerfilProductos								NVARCHAR(50);
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_Mod				 						BIGINT:=0;
			DECLARE v_IdSolicitud                                   BIGINT;
		
			--Variables Logs
			DECLARE v_fechaHoraSistema 								NVARCHAR(40);
			DECLARE v_NombreProceso									NVARCHAR(100):='Confirmar Solicitud de Cambio';
			DECLARE v_EstadoCliente									NVARCHAR(5):='';
			DECLARE	v_NombreCliente									NVARCHAR(100):='';
			DECLARE	v_IdDocumentoIdentidad							NVARCHAR(40):='';
			DECLARE	v_IdTipoDocumentoIdentidad						NVARCHAR(40):='';
			DECLARE v_EstadoPortafolio								NVARCHAR(5):='';
			DECLARE v_countPerfilCobro								BIGINT:=0;	
			DECLARE v_countPerfiles									BIGINT:=0;
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.	


		BEGIN
			v_IdEntidadOrigen:=CAST (p_IdPortafolio AS NVARCHAR(40));
			
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
		--Logica Negocio
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
			
			
					v_CampoModificado:='CodigoPortafolio';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."CodigoPortafolio" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdNombreFantasia';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdNombreFantasia" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='NumeroFicha';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."NumeroFicha" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdPersonaBrokerExtranjero';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPersonaBrokerExtranjero" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					
					v_CampoModificado:='IdEmpleadoEjecutivo';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdEmpleadoEjecutivo" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
						
							--Notificar cambio de ejecutivo
                        INSERT INTO "BSCL.SocioNegocio.BD::EJE.NotificarCambioEjecutivoCliente_Entrada"
                        SELECT 
                            "IdSolicitudCambio" AS "IdSolicitud", 
                            POR."IdCliente" AS "IdCliente", 
                            (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END) AS "IdPersonaNuevoEjecutivo"
                        FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
                        INNER JOIN :v_DatosSolicitudCambio MOD ON (POR."IdPortafolio" = cast (MOD."IdEntidadOrigen" as int))
                        WHERE MOD."CampoModificado" = V_CampoModificado AND MOD."IdTipoEntidad"= v_TipoEntidad;

                        CALL "BSCL.SocioNegocio.BD.Servicios::EJE.NotificarCambioEjecutivoCliente"(p_IdEmpresa,p_IdUsuario,p_IdModulo, :p_out_AdvertenciasContador, :p_out_AdvertenciasDetalle, :p_out_ErroresContador, :p_out_ErroresDetalle);
            
					END IF;

			
					v_CampoModificado:='DescripcionPortafolio';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."DescripcionPortafolio" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='Proposito';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."Proposito" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='Alias';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."Alias" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;


					v_CampoModificado:='IdPerfilComercialBaseOrigen';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilComercialBaseOrigen" = MOD."ValorNuevo",
							DOC."IdPerfilProductos" = PCOM."IdPerfilProductos",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
								INNER JOIN "BSCL.Plataforma.BD::COM.PerfilComercialBase" PCOM ON PCOM."IdPerfilComercialBase" = MOD."ValorNuevo"
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;

					v_CampoModificado:='IdPerfilProductos';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilProductos" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
						
						
						--	Log de ficha de cliente 
						
						SELECT 
						"IdCliente","IdCategoriaInversionista","IdPerfilProductos"  INTO v_IdCliente , v_IdCategoriaInversionista,v_IdPerfilProductos
						FROM  "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"  
						WHERE "IdPortafolio"= :p_IdPortafolio;
						 
						INSERT INTO "BSCL.SocioNegocio.BD::CLI.RegistroEncuesta" ("IdEmpresa", "IdCliente", "IdRegistroEncuesta", "IdCategoriaInversionista", "AsignadoPorEncuesta", "NombreEncuesta", "Puntaje", "FechaIngresoRegistro", "FechaEncuesta", "IdUsuarioResponde","IdCanal","IdPortafolio","IdPerfilProductos")
						VALUES(
						   :p_IdEmpresa
						  ,:v_IdCliente
						  ,"BSCL.SocioNegocio.BD.Secuencias::IdRegistroEncuesta".NEXTVAL
						  ,:v_IdCategoriaInversionista 
						  , 'P'
					      , ''
						  , 0
						  , CURRENT_UTCTIMESTAMP
						  , CURRENT_UTCTIMESTAMP
						  ,:p_IdUsuario
						  ,:p_IdCanal
						  ,:p_IdPortafolio
						  ,:v_IdPerfilProductos);
						
						
						
					END IF;
					
					v_CampoModificado:='IdPerfilProductosExtra';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilProductosExtra" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
						
						SELECT 
						"IdCliente","IdCategoriaInversionista","IdPerfilProductos"  INTO v_IdCliente , v_IdCategoriaInversionista,v_IdPerfilProductos
						FROM  "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"  
						WHERE "IdPortafolio"= :p_IdPortafolio;
						 
						INSERT INTO "BSCL.SocioNegocio.BD::CLI.RegistroEncuesta" ("IdEmpresa", "IdCliente", "IdRegistroEncuesta", "IdCategoriaInversionista", "AsignadoPorEncuesta", "NombreEncuesta", "Puntaje", "FechaIngresoRegistro", "FechaEncuesta", "IdUsuarioResponde","IdCanal","IdPortafolio","IdPerfilProductos")
						VALUES(
						   :p_IdEmpresa
						  ,:v_IdCliente
						  ,"BSCL.SocioNegocio.BD.Secuencias::IdRegistroEncuesta".NEXTVAL
						  ,:v_IdCategoriaInversionista 
						  , 'E'
					      , ''
						  , 0
						  , CURRENT_UTCTIMESTAMP
						  , CURRENT_UTCTIMESTAMP
						  ,:p_IdUsuario
						  ,:p_IdCanal
						  ,:p_IdPortafolio
						  ,:v_IdPerfilProductos);
						
						
					END IF;

					v_CampoModificado:='IdPerfilCobros';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilCobros" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdPerfilCobrosExtra';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilCobrosExtra" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;	
					
					
					v_CampoModificado:='IdPerfilCobrosPeru';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilCobrosPeru" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdPerfilCobrosPeruExtra';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilCobrosPeruExtra" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;					


					v_CampoModificado:='IdPerfilLimites';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilLimites" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;	
					

					v_CampoModificado:='IdPerfilLimitesExtra';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilLimitesExtra" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;						
	
	
					v_CampoModificado:='IdPerfilCondicionesOperativas';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilCondicionesOperativas" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;			

					v_CampoModificado:='IdPerfilCondicionesOperativasExtra';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdPerfilCondicionesOperativasExtra" = MOD."ValorNuevo",
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;						
					
					
					v_CampoModificado:='IdDireccionEnvioCorrespondencia';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdDireccionEnvioCorrespondencia" =  (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),	
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
										
					v_CampoModificado:='IdTelefonoContacto';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."IdTelefonoContacto" =  (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),	
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='AreaCliente';
					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad"= v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
		
						UPDATE  DOC
						SET
							DOC."AreaCliente" =  (CASE WHEN MOD."ValorNuevo"='' THEN 0 ELSE MOD."ValorNuevo" END),	
							DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
							
						FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
								INNER JOIN :v_DatosSolicitudCambio MOD ON (DOC."IdPortafolio"= cast (MOD."IdEntidadOrigen" as int))
						WHERE 
								MOD."CampoModificado" 		= v_CampoModificado
							AND	MOD."IdTipoEntidad"			= v_TipoEntidad
							AND DOC."IdPortafolio"	= cast (MOD."IdEntidadOrigen" as int);
							
							v_countProcesados:=v_countProcesados + 1;
					END IF;
					
					v_CampoModificado:='IdEmpleadoEjecutivoComercial';

					SELECT count("ValorNuevo") INTO v_count_Mod
					FROM :v_DatosSolicitudCambio 
					WHERE "CampoModificado" = v_CampoModificado
					AND "IdTipoEntidad" = v_TipoEntidad;
					
					IF (v_count_Mod > 0) THEN
					
					    UPDATE DOC
					    SET
					        DOC."IdEmpleadoEjecutivoComercial" = MOD."ValorNuevo",
					        DOC."FechaModificacion" = CURRENT_UTCTIMESTAMP
					        
					    FROM "BSCL.Portafolio.BD::POR.Portafolio" DOC
					    INNER JOIN :v_DatosSolicitudCambio MOD 
					        ON (DOC."IdPortafolio" = cast(MOD."IdEntidadOrigen" as int))
					    WHERE 
					            MOD."CampoModificado" = v_CampoModificado
					        AND MOD."IdTipoEntidad" = v_TipoEntidad
					        AND DOC."IdPortafolio" = cast(MOD."IdEntidadOrigen" as int);
					
					    v_countProcesados := v_countProcesados + 1;
					
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
			
				IF (p_IdPortafolio > 0) THEN
				
					SELECT 
						"CLI"."IdCliente"							
						,COALESCE("CLI"."NombreCompleto",'')					
						,"CLI"."DocumentoIdentidad"
						,"CLI"."TipoDocumentoIdentidad"
						,"CLI"."IdEstado"											
						,"PORT"."IdEstado"
					INTO 
						v_IdCliente
						,v_NombreCliente
						,v_IdDocumentoIdentidad
						,v_IdTipoDocumentoIdentidad
						,v_EstadoCliente
						,v_EstadoPortafolio
							FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" "PORT"
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" "CLI"
								ON ("PORT"."IdCliente" = "CLI"."IdCliente")
							WHERE "IdPortafolio" = :p_IdPortafolio;
				END IF; 

				--DATOS DETALLE LOG
				p_DetalleLog = (SELECT  
									MOD."IdTipoEntidad"		 	as "SubRegistro",
									MOD."CampoModificado"	 	as "CampoRegistro",
									CASE 	
										WHEN MOD."CampoModificado" = 'IdEmpleadoEjecutivo' AND COALESCE(MOD."ValorAntiguo",'') = CAST("EJEANT"."IdEmpleadoEjecutivo" AS NVARCHAR(40)) THEN "EJEANT"."NombreCompleto"
										WHEN MOD."CampoModificado" = 'IdDireccionEnvioCorrespondencia' AND COALESCE(MOD."ValorAntiguo",'') = CAST("DIRANT"."IdDireccionFisica" AS NVARCHAR(40)) THEN "DIRANT"."NombreLargo"
										WHEN MOD."CampoModificado" = 'IdTelefonoContacto' AND COALESCE(MOD."ValorAntiguo",'') = CAST("TELANT"."IdTelefono" AS NVARCHAR(40)) THEN "TELANT"."Numero"
										WHEN MOD."CampoModificado" = 'AreaCliente' AND COALESCE(MOD."ValorAntiguo",'') = "AREANT"."Codigo" THEN "AREANT"."Descripcion"
										WHEN MOD."CampoModificado" = 'IdPerfilCobros' AND COALESCE(MOD."ValorAntiguo",'') = "PERCOBANT"."IdPerfilCobros" THEN "PERCOBANT"."PerfilCobros" 
										ELSE MOD."ValorAntiguo" 
									END AS "ValorAntiguo",
									CASE 	
										WHEN MOD."CampoModificado" = 'IdEmpleadoEjecutivo' AND COALESCE(MOD."ValorNuevo",'') = CAST("EJENUE"."IdEmpleadoEjecutivo" AS NVARCHAR(40)) THEN "EJENUE"."NombreCompleto"
										WHEN MOD."CampoModificado" = 'IdDireccionEnvioCorrespondencia' AND COALESCE(MOD."ValorNuevo",'') = CAST("DIRNUE"."IdDireccionFisica" AS NVARCHAR(40)) THEN "DIRNUE"."NombreLargo"
										WHEN MOD."CampoModificado" = 'IdTelefonoContacto' AND COALESCE(MOD."ValorNuevo",'') = CAST("TELNUE"."IdTelefono" AS NVARCHAR(40)) THEN "TELNUE"."Numero"
										WHEN MOD."CampoModificado" = 'AreaCliente' AND COALESCE(MOD."ValorNuevo",'') = "ARENUE"."Codigo" THEN "ARENUE"."Descripcion"
										WHEN MOD."CampoModificado" = 'IdPerfilCobros' AND COALESCE(MOD."ValorNuevo",'') = "PERCOBNUE"."IdPerfilCobros" THEN "PERCOBNUE"."PerfilCobros"
										ELSE MOD."ValorNuevo" 
									END AS "ValorNuevo"
										FROM :v_DatosSolicitudCambio MOD
										LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarArea" 						AS "AREANT"	 	ON (COALESCE(MOD."ValorAntiguo",'') = "AREANT"."Codigo")
										LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarArea" 						AS "ARENUE" 	ON (COALESCE(MOD."ValorNuevo",'') = "ARENUE"."Codigo")
										LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo"					AS "EJEANT" 	ON (COALESCE(MOD."ValorAntiguo",'') = CAST("EJEANT"."IdEmpleadoEjecutivo" AS NVARCHAR(40)))
										LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" 				AS "EJENUE" 	ON (COALESCE(MOD."ValorNuevo",'') = CAST("EJENUE"."IdEmpleadoEjecutivo" AS NVARCHAR(40)))
										LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" 			AS "TELANT" 	ON (COALESCE(MOD."ValorAntiguo",'') = CAST("TELANT"."IdTelefono" AS NVARCHAR(40)))
										LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" 			AS "TELNUE" 	ON (COALESCE(MOD."ValorNuevo",'') = CAST("TELNUE"."IdTelefono" AS NVARCHAR(40)))
										LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 	AS "DIRANT" 	ON (COALESCE(MOD."ValorAntiguo",'') = CAST("DIRANT"."IdDireccionFisica" AS NVARCHAR(40)))
										LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" 	AS "DIRNUE" 	ON (COALESCE(MOD."ValorNuevo",'') = CAST("DIRNUE"."IdDireccionFisica" AS NVARCHAR(40)))
										LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobros"				AS "PERCOBANT"	ON(COALESCE(MOD."ValorAntiguo",'') = "PERCOBANT"."IdPerfilCobros")
										LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCobros"				AS "PERCOBNUE"	ON(COALESCE(MOD."ValorNuevo",'') = "PERCOBNUE"."IdPerfilCobros")
								);
							
				SELECT COUNT(*) INTO v_countPerfilCobro FROM :v_DatosSolicitudCambio MOD WHERE MOD."CampoModificado" = 'IdPerfilCobros';
				SELECT COUNT(*) INTO v_countPerfiles FROM :v_DatosSolicitudCambio MOD WHERE MOD."CampoModificado" <> 'IdPerfilCobros';
							
				IF(v_countPerfilCobro = 1 AND v_countPerfiles = 0) THEN
				
					v_TipoEntidad:='PERFILDECOBROS';
				
				END IF;

				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
			
				--GRABAR LOG
				CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => p_IdModulo
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => v_TipoEntidad--'PORTAFOLIO'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => :v_IdEntidadOrigen
					,p_IdPortafolio => :p_IdPortafolio
					,p_EstadoPortafolio => :v_EstadoPortafolio
					,p_EstadoProceso => :p_IdEstadoAutorizacion
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
