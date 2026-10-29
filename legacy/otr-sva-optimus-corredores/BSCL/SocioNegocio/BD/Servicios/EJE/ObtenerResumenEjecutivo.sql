SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.SocioNegocio.BD.Servicios::EJE.ObtenerResumenEjecutivo";
-- 
--  Proposito: Obtiene el Resumen de un ejecutivo en particular
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 27/09/2016
--   
CREATE PROCEDURE "BSCL.SocioNegocio.BD.Servicios::EJE.ObtenerResumenEjecutivo" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada 
	--IN  p_IdEmpleado				BIGINT,	--SI es 0 entonces se busca si el IdUsuario tiene ejecutivo

	-- Parametros salida del servicio publico
	OUT p_out_datos_ejecutivo		TABLE( "NombreEjecutivo" NVARCHAR(300),
											"Direccion" NVARCHAR(100),
											"Telefono" NVARCHAR(80),
											"JefeDirecto" NVARCHAR(300),
											"Supervisor" NVARCHAR(300)
											),
	OUT p_out_resumen_ejecutivo		TABLE( "AsuntosPendientes" BIGINT,
											"ClientesActivos" BIGINT,
											"OrdenesAccionesVigentes" BIGINT,
											"AutorizacionesPendientes" BIGINT,
											"PortafoliosActivos" BIGINT,
											"OrdenesFfmmVigentes" BIGINT
											),
		
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
		DECLARE p_IdEmpleado BIGINT DEFAULT 0;
		DECLARE v_tipoEntidad	NVARCHAR(3);
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla NVARCHAR(40);
			declare v_usuarioEmpleado BIGINT default 0;
						
			--Se esta preguntando por si un usuario es empleado.
			SELECT "IdEntidad", "TipoEntidad" INTO p_IdEmpleado, v_tipoEntidad 
			FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto"
			WHERE "IdUsuario" = p_IdUsuario;
			
			IF v_tipoEntidad <> 'EMP' THEN
				--El usuario no es empleado
				v_hash= select 'USUARIO' as "Codigo", '' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_USUARIO_NO_EMPLEADO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'UI_USUARIO_NO_EMPLEADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
			END IF;
			
			--Validar que el empleado asociado al usuario sea ejecutivo
			SELECT COUNT("IdEmpleado") INTO v_usuarioEmpleado
			FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" CEJ ON CE."IdEmpleado" = CEJ."IdEmpleadoEjecutivo"
			WHERE CE."IdEmpleado" = p_IdEmpleado AND CEJ."IdEstado" = 'ACT';
			
			IF v_usuarioEmpleado = 0 THEN
				--El usuario no es ejecutivo
				v_hash= select 'USUARIO' as "Codigo", '' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_USUARIO_NO_EJECUTIVO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'UI_USUARIO_NO_EJECUTIVO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
			END IF;
			
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;						  
			
		END;
	
		--------------------
		--Negocio Aqui
		--------------------
		BEGIN
			DECLARE v_asuntosPendientes BIGINT;
			DECLARE v_clientesActivos BIGINT;
			DECLARE v_ordenesAccionesVigentes BIGINT;
			DECLARE v_autorizacionesPendientes BIGINT;
			DECLARE v_portafoliosActivos BIGINT;
			DECLARE v_ordenesFfmmVigentes BIGINT;
			DECLARE v_fechaSistema BIGINT;
			
			--En el futuro quizas utilizar esta fecha para filtrar los asuntos, ordenes,ffmm, etc.
			--CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(p_IdEmpresa, 'FechaSistema', :v_fechaSistema);
			
			p_out_datos_ejecutivo = select TOP 1 CE."NombreCompleto" AS "NombreEjecutivo", "Direccion", "Numero" AS "Telefono", CE2."NombreCompleto" AS "JefeDirecto", CEJ."NombreSupervisor" AS "Supervisor" 
			FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" CPDE ON CE."IdPersona" = CPDE."IdPersona"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" CPT ON CE."IdPersona" = CPT."IdPersona"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE2 ON CE."IdEmpleadoSupervisor" = CE2."IdEmpleado"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" CEJ ON CE."IdEmpleado" = CEJ."IdEmpleadoEjecutivo"
			WHERE CE."IdEmpleado" = p_IdEmpleado ORDER BY "Direccion" DESC;

			--Construir el resumen
			SELECT 
			COUNT(CAP."IdDetalleAsuntoPendiente") INTO v_asuntosPendientes
			FROM "BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntosPendientes" CAP 
			WHERE CAP."IdEstadoDetalle" = 'SNC' AND CAP."IdEmpleado" = p_IdEmpleado AND CAP."IdEmpresa" = p_IdEmpresa;
			
			SELECT 
			COUNT(C."IdCliente") INTO v_clientesActivos
			FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS P
            		JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS C ON P."IdPersona" = C."IdPersona"
			WHERE C."IdEmpleadoEjecutivo" = :p_IdEmpleado AND C."IdEstado" = 'ACT' AND P."EsEnrolado" = 'S' AND C."IdEmpresa" = p_IdEmpresa;
			
			SELECT
			COUNT(CO."IdOrden") INTO v_ordenesAccionesVigentes
			FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" CO 
			WHERE CO."IdEmpleadoPortafolio" = p_IdEmpleado AND CO."IdEstadoOrden" = 'ING' AND CO."IdEmpresa" = p_IdEmpresa;
			
			SELECT 
			COUNT(CE."IdEvento") INTO v_autorizacionesPendientes
			FROM "BSCL.Plataforma.BD.Vistas::AUT.ConsultarEvento" CE 
			WHERE CE."IdUsuario" = p_IdUsuario AND CE."IdEstado" = 'VIG' AND CE."IdEmpresa" = p_IdEmpresa;
			
			SELECT
			COUNT(CP."IdPortafolio") INTO v_portafoliosActivos
			FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP 
			WHERE CP."IdEmpleadoEjecutivo" = p_IdEmpleado AND CP."IdEstado" = 'ACT';
			
			SELECT
			COUNT(CSAR."IdSolicitudAporteRescate") INTO v_ordenesFfmmVigentes
			FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" CSAR 
			WHERE CSAR."IdUsuario" = p_IdUsuario AND CSAR."EstadoSolicitud" <> 'ANU' AND CSAR."IdEmpresa" = p_IdEmpresa;
						
			p_out_resumen_ejecutivo = 							
			SELECT 
				v_asuntosPendientes AS "AsuntosPendientes",
				v_clientesActivos AS "ClientesActivos",
				v_ordenesAccionesVigentes AS "OrdenesAccionesVigentes",
				v_autorizacionesPendientes AS "AutorizacionesPendientes",
				v_portafoliosActivos AS "PortafoliosActivos",
				v_ordenesFfmmVigentes AS "OrdenesFfmmVigentes"
			FROM DUMMY WHERE 1=1;

		END;
	END;
END;