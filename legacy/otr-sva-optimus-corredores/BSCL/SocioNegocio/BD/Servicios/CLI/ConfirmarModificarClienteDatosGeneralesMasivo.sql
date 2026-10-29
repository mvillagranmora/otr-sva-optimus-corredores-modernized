SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::CLI.ConfirmarModificarClienteDatosGeneralesMasivo";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio para confirmar la modificacion a un cliente.
--  Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 22-12-2015
--  
"BSCL.SocioNegocio.BD.Servicios::CLI.ConfirmarModificarClienteDatosGeneralesMasivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_Cliente									TABLE ("IdCliente"	BIGINT),	
	IN  p_IdEstadoAutorizacion						NVARCHAR(3),
	IN  p_IdCanal									NVARCHAR(3) DEFAULT 'OPT',
	
	
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

			DECLARE v_countAutorizacionesPendientes					BIGINT;
			DECLARE v_IdEstadoAutorizacion_Calculado				NVARCHAR(3);
			DECLARE v_IdEntidadOrigen								NVARCHAR(40):='';
			DECLARE v_TipoEntidad									NVARCHAR(40):= 'CLIENTE';
			DECLARE V_CampoModificado								NVARCHAR(255):='';
			DECLARE v_countProcesados,v_count						BIGINT:=0;
			
			-- CUENTA DE CAMPOS A MODIFICAR.
			DECLARE v_count_IdEstado			 					BIGINT;
			DECLARE v_count_IdSegmentoCliente 						BIGINT;
			DECLARE v_count_IdCategoriaInversionista 				BIGINT;
			DECLARE v_count_IdPersonaEjecutivo						BIGINT;
			DECLARE v_count_IdTipoBloqueo							BIGINT;
			DECLARE v_count_FechaBloqueo							BIGINT;
			DECLARE v_count_ComentarioBloqueo						BIGINT;
			DECLARE v_count_CodigoEjecutivoExterno					BIGINT;
			DECLARE v_count_IdTipoCliente							BIGINT;
	
			DECLARE v_PerfilComercialPorDefecto 					table ("IdPerfilProductos" nvarchar(40), "IdPerfilComercialBase" nvarchar(40), "IdCategoriaInversionista" nvarchar(40));
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia de solicitud.		
		BEGIN
			--v_IdEntidadOrigen:=CAST (p_IdCliente AS NVARCHAR(40));
			SELECT COUNT("IdSolicitudCambio") INTO v_count
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
			JOIN :p_Cliente P ON  CAST(P."IdCliente" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
				AND SOL."IdTipoEntidad" 		= v_TipoEntidad
                AND SOL."IdEstadoAutorizacion"	='PEN';
                
                IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'SOLICITUD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;

			SELECT COUNT(CLI."IdCliente") INTO v_count
			FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
			JOIN :p_Cliente P ON P."IdCliente"= CLI."IdCliente";
				
                
                IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'ENTIDAD_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
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
            DECLARE v_ContadorSolicitudCambio BIGINT;
            DECLARE v_ContadorSolicitudEjecutivo BIGINT;
									
			--DATOS MAESTROS DE LA ENTIDAD

			v_DatosSolicitudCambio = SELECT SOL.* FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" SOL
				JOIN :p_Cliente P ON  CAST(P."IdCliente" AS NVARCHAR(40)) = SOL."IdEntidadOrigen"
				AND SOL."IdTipoEntidad" 		= v_TipoEntidad
                AND SOL."IdEstadoAutorizacion"	='PEN';
               
  			v_DatosClientes= SELECT CLI.* FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
			JOIN :p_Cliente P ON P."IdCliente"= CLI."IdCliente"; 

			v_DatosModificar = SELECT 
			  P."IdCliente"
			, CASE WHEN SOL."CampoModificado"='IdEstado'					THEN "ValorNuevo" ELSE NULL END AS "IdEstado"
			, CASE WHEN SOL."CampoModificado"='IdSegmentoCliente'			THEN "ValorNuevo" ELSE NULL END AS "IdSegmentoCliente"
			, CASE WHEN SOL."CampoModificado"='IdCategoriaInversionista'	THEN "ValorNuevo" ELSE NULL END AS "IdCategoriaInversionista"
			, CASE WHEN SOL."CampoModificado"='IdTipoBloqueo'				THEN "ValorNuevo" ELSE NULL END AS "IdTipoBloqueo"
			, CASE WHEN SOL."CampoModificado"='FechaBloqueo'				THEN "ValorNuevo" ELSE NULL END AS "FechaBloqueo"
			, CASE WHEN SOL."CampoModificado"='ComentarioBloqueo'			THEN "ValorNuevo" ELSE NULL END AS "ComentarioBloqueo"
			, CASE WHEN SOL."CampoModificado"='IdTipoCliente'				THEN "ValorNuevo" ELSE NULL END AS "IdTipoCliente"
			, CASE WHEN SOL."CampoModificado"='IdPersonaEjecutivo'			THEN "ValorNuevo" ELSE NULL END AS "IdPersonaEjecutivo"			
			, CASE WHEN SOL."CampoModificado"='CodigoEjecutivoExterno'		THEN "ValorNuevo" ELSE NULL END AS "CodigoEjecutivoExterno"						
			FROM :v_DatosSolicitudCambio SOL
			JOIN :v_DatosClientes P ON  CAST(P."IdCliente" AS NVARCHAR(40)) = SOL."IdEntidadOrigen";

			v_DatosModificarAgrupado =
				SELECT
				  			CLI."IdCliente"
							, COALESCE(GRU."IdEstado", CLI."IdEstado")									AS "IdEstado"
							, COALESCE(GRU."IdSegmentoCliente", CLI."IdSegmentoCliente")				AS "IdSegmentoCliente"
							, COALESCE(GRU."IdCategoriaInversionista", CLI."IdCategoriaInversionista")	AS "IdCategoriaInversionista"
							, COALESCE(GRU."IdTipoBloqueo", CLI."IdTipoBloqueo")						AS "IdTipoBloqueo"
							, COALESCE(GRU."FechaBloqueo", CLI."FechaBloqueo")							AS "FechaBloqueo"
							, COALESCE(GRU."ComentarioBloqueo", CLI."ComentarioBloqueo")				AS "ComentarioBloqueo"
							, COALESCE(GRU."IdTipoCliente", CLI."IdTipoCliente")						AS "IdTipoCliente"
							, COALESCE(GRU."IdPersonaEjecutivo", CLI."IdPersonaEjecutivo")				AS "IdPersonaEjecutivo"							
							, COALESCE(GRU."CodigoEjecutivoExterno", CLI."CodigoEjecutivoExterno")		AS "CodigoEjecutivoExterno"														
				FROM
						(
						SELECT 
							  SOL."IdCliente"
							, MAX(SOL."IdEstado") 					AS "IdEstado"
							, MAX(SOL."IdSegmentoCliente")			AS "IdSegmentoCliente"
							, MAX(SOL."IdCategoriaInversionista")	AS "IdCategoriaInversionista"
							, MAX(SOL."IdTipoBloqueo")				AS "IdTipoBloqueo"
							, MAX(SOL."FechaBloqueo")				AS "FechaBloqueo"
							, MAX(SOL."ComentarioBloqueo")			AS "ComentarioBloqueo"
							, MAX(SOL."IdTipoCliente")				AS "IdTipoCliente"
							, MAX(SOL."IdPersonaEjecutivo")			AS "IdPersonaEjecutivo"							
							, MAX(SOL."CodigoEjecutivoExterno")		AS "CodigoEjecutivoExterno"														
							
							FROM :v_DatosModificar  SOL GROUP BY SOL."IdCliente"
						) GRU
				JOIN :v_DatosClientes  CLI ON  CLI."IdCliente"  = GRU."IdCliente";			

			
			v_ClientesModificadosCategoria = SELECT CLI."IdCliente",PORT."IdPortafolio",MODCLI."IdCategoriaInversionista",PORT."IdPerfilProductos"
											FROM :v_DatosModificarAgrupado MODCLI 
											INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON MODCLI."IdCliente" = CLI."IdCliente"
											INNER JOIN  "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio"  PORT ON PORT."IdCliente" = CLI."IdCliente"
											WHERE MODCLI."IdCategoriaInversionista" != CLI."IdCategoriaInversionista";
										
				INSERT INTO "BSCL.SocioNegocio.BD::CLI.RegistroEncuesta" ("IdEmpresa", "IdCliente", "IdRegistroEncuesta", "IdCategoriaInversionista", "AsignadoPorEncuesta", "NombreEncuesta", "Puntaje", "FechaIngresoRegistro", "FechaEncuesta", "IdUsuarioResponde","IdCanal","IdPortafolio","IdPerfilProductos")
				SELECT
				   :p_IdEmpresa AS "IdEmpresa"
				  ,"IdCliente"
				  ,"BSCL.SocioNegocio.BD.Secuencias::IdRegistroEncuesta".NEXTVAL AS "IdRegistroEncuesta"
				  , "IdCategoriaInversionista"
				  ,'N'AS "AsignadoPorEncuesta"
			      ,'' AS "NombreEncuesta"
				  ,0  AS "Puntaje"
				  ,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"
				  ,CURRENT_UTCTIMESTAMP AS "FechaEncuesta"
				  ,:p_IdUsuario AS "IdUsuarioResponde"
				  ,:p_IdCanal AS "IdCanal"
				  ,"IdPortafolio"
				  ,"IdPerfilProductos"
				  FROM :v_ClientesModificadosCategoria;
										
			
			--ACTUALIZACION DE VALORES
			UPDATE  CLI
				SET
					CLI."IdEstado"			 		= MOD."IdEstado",
					CLI."IdSegmentoCliente"			= MOD."IdSegmentoCliente",
					CLI."IdCategoriaInversionista" 	= MOD."IdCategoriaInversionista",
					CLI."IdTipoBloqueo" 			= MOD."IdTipoBloqueo",
					CLI."FechaBloqueo" 				= MOD."FechaBloqueo",
					CLI."ComentarioBloqueo" 		= MOD."ComentarioBloqueo",
					CLI."IdTipoCliente" 			= MOD."IdTipoCliente",
					--CLI."IdPersonaEjecutivo" 		= (CASE WHEN MOD."IdPersonaEjecutivo"='' THEN 0 ELSE cast (MOD."IdPersonaEjecutivo" as int) END),
					CLI."IdPersonaEjecutivo" 		= MOD."IdPersonaEjecutivo",
					CLI."CodigoEjecutivoExterno" 	= UPPER(MOD."CodigoEjecutivoExterno"),
					CLI."FechaModificacion"			= CURRENT_UTCTIMESTAMP
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
						INNER JOIN :v_DatosModificarAgrupado MOD ON (CLI."IdCliente"= MOD."IdCliente");


			SELECT count("ValorNuevo") INTO v_count_IdCategoriaInversionista
			FROM :v_DatosSolicitudCambio WHERE "CampoModificado" = 'IdCategoriaInversionista'
			AND "IdTipoEntidad"= v_TipoEntidad;
		
			IF (v_count_IdCategoriaInversionista > 0) THEN
				BEGIN
					---------------------------------------------------------------------------------------------------------
					-- asigna el perfil comercial por defecto y el producto de la categoria de inversionista en el portafolio
					---------------------------------------------------------------------------------------------------------
					v_PerfilComercialPorDefecto = SELECT PCOM."IdPerfilProductos", PCOM."IdPerfilComercialBase", MOD."ValorNuevo" AS "IdCategoriaInversionista" 
					FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" PCOM
					INNER JOIN :v_DatosSolicitudCambio MOD ON (MOD."ValorNuevo" = PCOM."IdCategoriaInversionista")
					WHERE PCOM."PorDefecto" = 'S'
					LIMIT 1;
						 
					UPDATE PORT
						SET   
							PORT."IdPerfilComercialBaseOrigen" = PCOM."IdPerfilComercialBase",
							PORT."IdPerfilProductos" = PCOM."IdPerfilProductos",
							PORT."FechaModificacion" = CURRENT_UTCTIMESTAMP
					FROM "BSCL.Portafolio.BD::POR.Portafolio" PORT
							INNER JOIN :v_DatosSolicitudCambio MOD ON (PORT."IdCliente"= cast (MOD."IdEntidadOrigen" as int))
							INNER JOIN :v_PerfilComercialPorDefecto PCOM ON PCOM."IdCategoriaInversionista" = MOD."ValorNuevo" 
					WHERE 
							PORT."IdCliente"	= cast (MOD."IdEntidadOrigen" as int);
					END;				
			END IF;
								
			--Se termina el flujo con la actualizacion del registro de solicitud.
			v_SolicitudCambio = SELECT 0 as "IdSolicitudCambio" FROM dummy where 1=2;
			v_EntidadOrigen = SELECT "IdEntidadOrigen", "IdTipoEntidad" FROM :v_DatosSolicitudCambio;
			CALL "BSCL.Plataforma.BD.Servicios::PLA.ModificarEstadoRegistroSolicitudCambioMasivo" (
				p_IdEmpresa				=>:p_IdEmpresa,
				p_IdUsuario				=>:p_IdUsuario,
				p_IdModulo				=>:p_IdModulo,
				p_SolicitudCambio			=>:v_SolicitudCambio,
				p_EntidadOrigen			    =>:v_EntidadOrigen,
				p_IdEstadoAutorizacion	=>:p_IdEstadoAutorizacion,
				p_out_AdvertenciasContador		=>:p_out_AdvertenciasContador,
				p_out_AdvertenciasDetalle		=>:p_out_AdvertenciasDetalle,	
				p_out_ErroresContador			=>:p_out_ErroresContador,
				p_out_ErroresDetalle			=>:p_out_ErroresDetalle				
				
			);
			
			BEGIN
				--NOTIFICAR
			    DECLARE v_ContadorSolicitudCambio BIGINT;
                DECLARE v_ContadorSolicitudEjecutivo BIGINT;
				
				v_DatosSolicitudCambioEjecutivo = SELECT SOL.* FROM :v_DatosSolicitudCambio SOL WHERE SOL."CampoModificado" IN ('IdPersonaEjecutivo');
				v_DatosSolicitudCambio = SELECT SOL.* FROM :v_DatosSolicitudCambio SOL WHERE SOL."CampoModificado" NOT IN ('IdPersonaEjecutivo');                
                
				SELECT COUNT(*) INTO v_ContadorSolicitudCambio FROM :v_DatosSolicitudCambio;
				SELECT COUNT(*) INTO v_ContadorSolicitudEjecutivo FROM :v_DatosSolicitudCambioEjecutivo;
                
				IF v_ContadorSolicitudCambio > 0 THEN
				
    				INSERT INTO "BSCL.SocioNegocio.BD::EJE.NotificarCambioInformacionFichaCliente_Entrada"
                    SELECT SOL."IdSolicitudCambio" AS "IdSolicitud", CLI."IdCliente", SOL."CampoModificado", CLI."IdEstado" AS "ValorAntiguo", SOL."ValorNuevo"
                    FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
                    INNER JOIN :v_DatosSolicitudCambio SOL ON (CLI."IdCliente"= cast (SOL."IdEntidadOrigen" as int))
                    WHERE SOL."IdTipoEntidad"= v_TipoEntidad;
    
    				v_clientes = SELECT cast ("IdEntidadOrigen" as int) AS "IdCliente", "IdSolicitudCambio" AS "IdSolicitud" FROM :v_DatosSolicitudCambio;
                    CALL "BSCL.SocioNegocio.BD.Servicios::EJE.NotificarCambioInformacionFichaCliente"(p_IdEmpresa,p_IdUsuario,p_IdModulo, :v_clientes, :p_out_AdvertenciasContador, :p_out_AdvertenciasDetalle, :p_out_ErroresContador, :p_out_ErroresDetalle);
                    
                END IF;
                
                IF v_ContadorSolicitudEjecutivo > 0 THEN
                    --Notificar cambio de ejecutivo
                    INSERT INTO "BSCL.SocioNegocio.BD::EJE.NotificarCambioEjecutivoCliente_Entrada"
                    SELECT 
                        SOL."IdSolicitudCambio" AS "IdSolicitud", 
                        CLI."IdCliente" AS "IdCliente", 
                        CEMP."IdEmpleado" AS "IdPersonaNuevoEjecutivo"
                    FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
                    INNER JOIN :v_DatosSolicitudCambioEjecutivo SOL ON (CLI."IdCliente"= cast (SOL."IdEntidadOrigen" as int))
                    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CEMP ON CEMP."IdPersona" = SOL."ValorNuevo"
                    WHERE SOL."IdTipoEntidad"= v_TipoEntidad;
        
                    CALL "BSCL.SocioNegocio.BD.Servicios::EJE.NotificarCambioEjecutivoCliente"(p_IdEmpresa,p_IdUsuario,p_IdModulo, :p_out_AdvertenciasContador, :p_out_AdvertenciasDetalle, :p_out_ErroresContador, :p_out_ErroresDetalle);
        
                END IF;
			END;

			--LOG DE AUDITORIA (inicio)
			BEGIN 
				DECLARE v_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
				DECLARE v_EstadoCliente	NVARCHAR(3);
				DECLARE	v_NombreCliente	NVARCHAR(100);
				DECLARE	v_IdTipoDocumentoIdentidad	NVARCHAR(40);
				DECLARE	v_IdDocumentoIdentidad	NVARCHAR(40);
				DECLARE v_fechaHoraSistema NVARCHAR(40);
				DECLARE v_NombreProceso	NVARCHAR(100):='Confirmar Solicitud de Cambio';
				
				
				--DATOS CABECERA LOG
	
				SELECT TOP 1 CLI."NombreCompleto", 
				CLI."TipoDocumentoIdentidad",
				CLI."DocumentoIdentidad",
				CLI."IdEstado",
				CLI."IdCliente"
				
				INTO v_NombreCliente,
				v_IdTipoDocumentoIdentidad,
				v_IdDocumentoIdentidad,
				v_EstadoCliente,
				v_IdEntidadOrigen
				FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
					JOIN :p_Cliente P ON P."IdCliente"= CLI."IdCliente";
	
				call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
					p_IdEmpresa => p_IdEmpresa,
					p_out_FechaHoraSistema => v_fechaHoraSistema
				);
				
				--DATOS DETALLE LOG
				p_DetalleLog = 
				(Select MOD."IdTipoEntidad"		 as "SubRegistro",
						MOD."CampoModificado"	 as "CampoRegistro",
						MOD."ValorAntiguo"  	 as "ValorAntiguo",
						MOD."ValorNuevo" 		 as "ValorNuevo" 
				FROM :v_DatosSolicitudCambio MOD);
				


				--GRABAR LOG

					CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'MODIFICACION' 
					,p_IdModulo => 'SOC' 
					,p_IdUsuario => p_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => p_IdUsuario
					,p_IdTipoRegistro => v_TipoEntidad--'CLIENTE'
					,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
					,p_NombreProceso => :v_NombreProceso-- hacia abajo opcionales
					,p_IdRegistro => '0'
					,p_EstadoProceso => :p_IdEstadoAutorizacion
					,p_EstadoRegistro=> :p_IdEstadoAutorizacion
					,p_EstadoCliente=> :v_EstadoCliente --Cliente
					,p_IdCliente => :v_IdEntidadOrigen
					,p_NombreCliente=> :v_NombreCliente
					,p_IdTipoDocumentoIdentidad=> :v_IdTipoDocumentoIdentidad
					,p_IdDocumentoIdentidad=> :v_IdDocumentoIdentidad					
					);
	
			END;
			--LOG DE AUDITORIA (Fin)			
		
			
			
						
		END;

		--------------------
		--FIN SP 
		--------------------
	END;	
END;
