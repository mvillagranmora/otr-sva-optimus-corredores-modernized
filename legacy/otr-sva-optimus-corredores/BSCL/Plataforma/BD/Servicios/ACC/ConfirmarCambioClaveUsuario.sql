SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.ConfirmarCambioClaveUsuario";

CREATE PROCEDURE 
-- 
--  Proposito: Solicitud recuperacion de password usuario
--  Autor: - BCS
--  Fecha Creacion: 23/12/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.ConfirmarCambioClaveUsuario" (
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_Username			NVARCHAR(100),
	IN p_Password			NVARCHAR(520),
	IN p_Token				NVARCHAR(100),
		
	OUT p_StatusCode		INT,
		
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
		v_hash= select 'ERROR' as "Codigo", ''  as "Valor" FROM DUMMY;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ERROR_CONFIRMAR_CLAVE',:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  select 'ERROR_CONFIRMAR_CLAVE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
								select * from :p_out_ErroresDetalle; 		 					 	
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;	
		p_StatusCode:=1;							
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_IdUsuario BIGINT;	
		DECLARE v_IdEntidad BIGINT;
		DECLARE v_TipoEntidad NVARCHAR(3);
		DECLARE v_Existe 	BIGINT;
		DECLARE v_Token		NVARCHAR(100);
		DECLARE v_SysDate 			NVARCHAR(20);
		DECLARE v_currentTimestamp SECONDDATE;
		--Variables notificacion
		DECLARE p_Eventos TABLE("CodigoTipoEvento" nvarchar(40), "IdSecuencia" BIGINT , "FechaCreacionSistema" SECONDDATE, "IdPersonaRemitente" BIGINT, "IdPersonaDestinatario" BIGINT, "IdPortafolio" BIGINT) DEFAULT SELECT '' AS "CodigoTipoEvento", 0 AS"IdSecuencia", CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 0 AS "IdPersonaRemitente", 0 AS "IdPersonaDestinatario", 0 AS "IdPortafolio" FROM DUMMY WHERE 1=2;
		DECLARE p_EventoDetalle TABLE("IdSecuencia"  BIGINT,"Llave"	 NVARCHAR(40),"Valor"	NVARCHAR(300));
		DECLARE p_ArchivosAdjuntos TABLE("IdSecuencia"  BIGINT,"ArchivoAdjunto"	 BLOB,"Nombre" NVARCHAR(520),"Contrasenia" NVARCHAR(520));
		DECLARE p_IdDirecciones TABLE ("IdSecuencia" BIGINT, "IdDireccion" BIGINT,"TipoDestinatario" NVARCHAR(6));
		DECLARE p_out_IdEvento TABLE ("IdEvento" BIGINT,"IdSecuencia" BIGINT);
		--Variables log
		DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE p_timestampValidacionUsuario		timestamp default CURRENT_UTCTIMESTAMP; 
		DECLARE p_SesionAntigua TABLE ("IdTokenSesion" nvarchar(520), "IdUsuario" BIGINT, "TokenValido" NVARCHAR(1), "FechaUltimoUso" TIMESTAMP, "UsoToken" INTEGER, "FechaIngreso" TIMESTAMP, "FechaSalida" TIMESTAMP);
		-----------------------------------------------------
		--Inicializacion Variables
		-----------------------------------------------------
		SELECT CURRENT_UTCTIMESTAMP INTO v_currentTimestamp FROM DUMMY;
		SELECT CURRENT_DATE into v_SysDate from DUMMY;
		-----------------------------------------------------
		--Logica de negocio
		-----------------------------------------------------
		SELECT COUNT("IdUsuario") INTO v_Existe FROM "BSCL.Plataforma.BD::ACC.Usuario" WHERE "CodigoUsuario" = p_Username;
		SELECT * FROM "BSCL.Plataforma.BD::ACC.Usuario" WHERE "CodigoUsuario" = p_Username;
		IF v_Existe > 0 THEN
		
			SELECT "IdUsuario", "IdEntidad", "TipoEntidad" INTO v_IdUsuario, v_IdEntidad, v_TipoEntidad
			FROM "BSCL.Plataforma.BD::ACC.Usuario" 
			WHERE "CodigoUsuario" = p_Username;
			
			SELECT COUNT("Token") INTO v_Existe
			FROM "BSCL.Plataforma.BD::ACC.RecuperarClave" 
			WHERE "IdEstado" = 'ACT' AND "IdUsuario" = v_IdUsuario AND "IdEmpresa" = p_IdEmpresa AND v_currentTimestamp BETWEEN "ValidoDesde" AND "ValidoHasta";
			
			SELECT *
			FROM "BSCL.Plataforma.BD::ACC.RecuperarClave" 
			WHERE "IdEstado" = 'ACT' AND "IdUsuario" = v_IdUsuario AND "IdEmpresa" = p_IdEmpresa AND v_currentTimestamp BETWEEN "ValidoDesde" AND "ValidoHasta";
			
			
			IF v_Existe > 0 THEN
				--TIENE UN TOKEN VALIDO REENVIAR.
				SELECT "Token" INTO v_Token
				FROM "BSCL.Plataforma.BD::ACC.RecuperarClave" 
				WHERE "IdEstado" = 'ACT' AND "IdUsuario" = v_IdUsuario AND "IdEmpresa" = p_IdEmpresa AND v_currentTimestamp BETWEEN "ValidoDesde" AND "ValidoHasta";
				select v_Token AS "asd", p_Token AS "asd2" from dummy;
				IF p_Token = v_Token THEN
					
					UPDATE "BSCL.Plataforma.BD::ACC.Usuario" 
					SET "Clave" = HASH_SHA256(to_binary(p_Password)),
						"DebeCambiarClave" = 'N',
						"UltimoCambioClave" = v_currentTimestamp
					WHERE "IdUsuario" = v_IdUsuario;
					p_StatusCode := 0;

					--invalidar todos los tokens del usuario
					UPDATE "BSCL.Plataforma.BD::ACC.RecuperarClave" 
					SET "IdEstado" = 'INA' 
					WHERE "IdEstado" = 'ACT' AND "IdUsuario" = v_IdUsuario AND "IdEmpresa" = p_IdEmpresa;
					
					--NOTIFICAR EL CAMBIO DE CLAVE

					IF v_TipoEntidad = 'CLI' THEN
						v_usuario_notificar = SELECT
						"BSCL.Plataforma.BD.Secuencias::EventoNotificacion".NEXTVAL AS "IdSecuencia",
						CC."IdPersona",
						CC."IdDireccionElectronicaPrincipal",
						CC."NombreCompleto"			 
						FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
						WHERE "IdCliente" = v_IdEntidad;
						
						p_Eventos = SELECT 
						'CLAVE_CLIENTE_RECUPERADA' AS "CodigoTipoEvento", 
						UN."IdSecuencia", 
						CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
						0 AS "IdPersonaRemitente", 
						UN."IdPersona" AS "IdPersonaDestinatario",
						0 AS "IdPortafolio"
						FROM :v_usuario_notificar UN;
						
					ELSEIF v_TipoEntidad = 'EMP' THEN
						v_usuario_notificar = SELECT
						"BSCL.Plataforma.BD.Secuencias::EventoNotificacion".NEXTVAL AS "IdSecuencia",
						CE."IdPersona",
						CE."IdDireccionElectronicaPrincipal",
						CE."NombreCompleto"			 
						FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
						WHERE "IdEmpleado" = v_IdEntidad;
						
						p_Eventos = SELECT 
						'CLAVE_EMPLEADO_RECUPERADA' AS "CodigoTipoEvento", 
						UN."IdSecuencia", 
						CURRENT_UTCTIMESTAMP AS "FechaCreacionSistema", 
						0 AS "IdPersonaRemitente", 
						UN."IdPersona" AS "IdPersonaDestinatario",
						0 AS "IdPortafolio"
						FROM :v_usuario_notificar UN;
					END IF;
								
					p_IdDirecciones = 
					SELECT 
					E."IdSecuencia",	
					"IdDireccionElectronicaPrincipal" AS "IdDireccion",
					'TO' AS "TipoDestinatario"
					FROM :v_usuario_notificar E;
		
					p_ArchivosAdjuntos = SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;
								
					p_EventoDetalle = SELECT "IdSecuencia", 'Nombre' AS "Llave", "NombreCompleto" AS "Valor" FROM :v_usuario_notificar
					UNION ALL SELECT "IdSecuencia", 'Token' AS "Llave", p_Token AS "Valor" FROM :v_usuario_notificar;
					
					--Guardar eventos
					CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
			 			p_IdEmpresa => p_IdEmpresa,
			 			p_IdUsuario => v_IdUsuario,
			 			p_IdModulo => 'PLA',
			 			p_Eventos => :p_Eventos,
			 			p_Valores => :p_EventoDetalle,
			 			p_ArchivosAdjuntos => :p_ArchivosAdjuntos,
			 			p_IdDirecciones => :p_IdDirecciones,
			 			p_out_IdEvento => p_out_IdEvento
			 		);
					
					--ALMACENAR LOG POR CAMBIO DE CLAVE
					p_DetalleLog =  select upper(p_Username) as "SubRegistro", 'Clave' as "CampoRegistro", 'CAMBIO CLAVE RECUPERADA' as "ValorAntiguo", '*****' as "ValorNuevo" FROM dummy;
					
					CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					p_IdTipoLog => 'ACTUALIZACION' 
					,p_IdModulo => 'PLA' 
					,p_IdUsuario => v_IdUsuario 
					,p_IdEmpresa => p_IdEmpresa 
					,p_FechaSistemaLog => TO_VARCHAR (v_SysDate, 'YYYY-MM-DD')
					,p_IdUsuarioAtendido => v_IdUsuario
					,p_IdTipoRegistro => 'USUARIO'
					,p_DetalleLog => :p_DetalleLog
					);
					
				ELSE
					SIGNAL SALIDA_CONTROLADA;
				END IF;
			ELSE
				SIGNAL SALIDA_CONTROLADA;
			END IF;

		ELSE
			--USUARIO NO EXISTE
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		--------------------
		--FIN SP 
		--------------------
	END;
END;
