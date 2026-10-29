SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.CrearUsuario";
-- 
--  Proposito: Servicio para la creacion de usuarios.
--  Autor: - CHERNANDEZ
--  Fecha Creacion: 14/01/2016
--  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.CrearUsuario" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para la creacion del usuario
	IN  p_CodigoUsuario				nvarchar(40),  		
	IN  p_Clave						nvarchar(520),  	
	IN  p_FechaIngreso				nvarchar(10),  	
	IN  p_FechaVigencia				nvarchar(10),
	IN  p_FechaUltimaConexion		nvarchar(10),
	IN	p_Conectado					nvarchar(520),
	IN	p_Hora						nvarchar(8),
	IN	p_Terminal					nvarchar(520),
	IN	p_IdEstado					nvarchar(3),
	In	p_IntentosConexion			integer,
	IN	p_FechaBloqueoTempotal		nvarchar(20),
	IN	p_TipoEntidad				nvarchar(40),
	IN	p_IdEntidad					bigint,
	IN  p_HeredarRolesUsuario       bigint,
	--parametros configuracion usuario
	IN	p_UsuarioCorredores			nvarchar(1),
	IN	p_UsuarioNegocios			nvarchar(1),
	IN	p_UsuarioClientes			nvarchar(1),
	IN	p_UsuarioServicios			nvarchar(1),

	-- Parametros salida del servicio publico
	OUT p_out_new_user_id bigint,
		
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
		DECLARE v_RequiereAutorizacion				NVARCHAR(1);
		DECLARE v_EsEnrolado						NVARCHAR(1);
		DECLARE v_CountHeredarRolesUsuario          BIGINT;
		DECLARE v_DiasVencimientoClave              BIGINT;
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
			declare v_cantidadSocioNegocios integer;
			DECLARE v_Quantity BIGINT;
			DECLARE v_EsEquipoSeguridad	BIGINT;
			
			--Verifica si el socio de negocio IdEmpresa+TipoEntidad+IdEntidad existen en la tabla
 			if p_TipoEntidad = 'CLI' then
				SELECT COUNT("IdSocioDeNegocio")
				INTO v_Quantity
			    FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as Usu
			    WHERE "IdCliente" = p_IdEntidad;
			else
				if p_TipoEntidad = 'EMP' then
					SELECT COUNT("IdSocioDeNegocio")
					INTO v_Quantity
				    FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as Usu
				    WHERE "IdEmpleado" = p_IdEntidad;
				end if;
			end if;
		    IF :v_Quantity = 0 THEN			
			v_hash= select 'SOCIONEGOCIO' as "Codigo", '' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('NO_EXISTE_SOCIO_NEGOCIO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'NO_EXISTE_SOCIO_NEGOCIO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;	 					  
			END IF;
			
			--Verifica si el usuario idEmpresa+CodigoUsuario+TipoEntidad ya existe en la tabla de usuarios.
			v_Quantity := 0;
				   	
			SELECT COUNT("IdUsuario")
			INTO v_Quantity
		    FROM "BSCL.Plataforma.BD::ACC.Usuario" as Usu
		    WHERE "IdEmpresa" = p_IdEmpresa and "CodigoUsuario" = upper(p_codigoUsuario);

			IF :v_Quantity >0 THEN
				v_hash= select 'USUARIO' as "Codigo", '' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('USUARIO_YA_EXISTE',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'USUARIO_YA_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;				
			END IF;
			
			SELECT COUNT("IdUsuario")
			into v_cantidadSocioNegocios 
			FROM "BSCL.Plataforma.BD::ACC.Usuario"
			WHERE "IdEmpresa" = p_IdEmpresa and "TipoEntidad" = p_TipoEntidad and "IdEntidad" = p_IdEntidad;
			
			IF :v_cantidadSocioNegocios  > 0 THEN
				v_hash= select 'SOCIO' as "Codigo", '' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_SOCIO_ASOCIADO_A_USUARIO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'UI_SOCIO_ASOCIADO_A_USUARIO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
			END IF;
			
			
			--Rol 2 es por defecto equipo de seguridad.
			select COUNT("IdUsuario") INTO v_EsEquipoSeguridad 
			FROM "BSCL.Plataforma.BD::ACC.RolUsuario" 
			where "IdUsuario" = p_IdUsuario and "IdRol" = 2;
			--Solo pueden crear usuarios los usuarios con rol 2:Seguridad
			IF v_EsEquipoSeguridad < 1 AND p_TipoEntidad <> 'EMP' THEN
				v_hash= select 'SOCIO' as "Codigo", '' as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_NO_EQUIPO_SEGURIDAD',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'UI_NO_EQUIPO_SEGURIDAD' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle;
			END IF;

			--Validar que se quiera heredarRoles para el usuario
			SELECT COUNT(1) INTO v_CountHeredarRolesUsuario FROM "BSCL.Plataforma.BD::ACC.Usuario"
            WHERE "IdUsuario" = :p_HeredarRolesUsuario AND "IdEmpresa" = :p_IdEmpresa;
                        
            IF v_CountHeredarRolesUsuario = 0 AND :p_HeredarRolesUsuario <> -1 THEN
                v_hash= select 'SOCIO' as "Codigo", '' as "Valor" FROM DUMMY;
                CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_HEREDAR_ROLES_ERROR',:v_hash,:v_erroroutmsg);
                p_out_ErroresDetalle =  select 'UI_HEREDAR_ROLES_ERROR' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
                                        select * from :p_out_ErroresDetalle;
            END IF;

			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;						  
			
		END;
		------------------------------------------
		--Pregunta Si requiere autorizacion
		------------------------------------------
		
		SELECT "RequiereAutorizacion" INTO v_RequiereAutorizacion 
		FROM "BSCL.Plataforma.BD::PLA.RequiereAutorizacion" 
		WHERE "IdConcepto" = 'ENROLU' and "IdEmpresa" = p_IdEmpresa;
		
		if v_RequiereAutorizacion='S' then
			v_EsEnrolado		:=  'P';
		else
			v_EsEnrolado		:=  'S';
		end if;
		
		
		--------------------
		--Negocio Aqui
		--------------------
		SELECT "BSCL.Plataforma.BD.Secuencias::Usuario".NEXTVAL INTO p_out_new_user_id FROM DUMMY;
		SELECT HASH_SHA256(to_binary(p_Clave)) "CLAVE" INTO p_Clave FROM dummy;
				
		SELECT COALESCE("Valor",30) INTO v_DiasVencimientoClave FROM "BSCL.Plataforma.BD::PLA.Configuracion" 
									 WHERE "IdEmpresa" = :p_IdEmpresa 
									 AND "CodigoParametro" = 'PLA.DiasCambioClave';
		
		INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" 
		(
			"IdUsuario",
			"IdEmpresa",
			"CodigoUsuario",
			"Clave",
			"FechaIngreso",
			"FechaVigencia",
			"FechaUltimaConexion",
			"DiasVencimientoClave",
			"Conectado",
			"Hora",
			"Terminal",
			"IdEstado",
			"IntentosConexion",
			"FechaBloqueoTemporal",
			"TipoEntidad",
			"IdEntidad",
			"EsAutorizado",
			"UsuarioCorredores",
			"UsuarioNegocios",
			"UsuarioClientes",
			"UsuarioServicios"
		)  
		VALUES
		(
			p_out_new_user_id,
			p_IdEmpresa,
			upper(p_CodigoUsuario),
			p_Clave,
			TO_DATE(p_FechaIngreso, 'YYYY-MM-DD'),
			TO_DATE(p_FechaVigencia, 'YYYY-MM-DD'),
			TO_DATE(p_FechaUltimaConexion, 'YYYY-MM-DD'),
			:v_DiasVencimientoClave,
			p_Conectado,
			TO_TIME(p_Hora, 'HH24:MI:SS'),
			p_Terminal,
			p_IdEstado,
			p_IntentosConexion,
			CURRENT_UTCTIMESTAMP,
			p_TipoEntidad,
			p_IdEntidad,
			v_EsEnrolado,
			p_UsuarioCorredores,
			p_UsuarioNegocios,
			p_UsuarioClientes,
			p_UsuarioServicios
			
		);
		
		--Comprobar si heredara los roles de otro usuario
        IF  v_CountHeredarRolesUsuario > 0 AND :p_HeredarRolesUsuario <> -1 THEN
            INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario"("IdRolUsuario", "IdUsuario", "IdRol", "EsAutorizado")
            SELECT 
            "BSCL.Plataforma.BD.Secuencias::RolUsuario".NEXTVAL AS "IdRolUsuario",
            p_out_new_user_id AS "IdUsuario",
            RU."IdRol",
            'S' AS "EsAutorizado"
            FROM  "BSCL.Plataforma.BD::ACC.RolUsuario" RU
            WHERE RU."IdUsuario" = :p_HeredarRolesUsuario;
            
            --HEREDAR MENU DE USUARIO
            UPDATE U 
			SET U."CodigoMenu" = U1."CodigoMenu"
			FROM "BSCL.Plataforma.BD::ACC.Usuario" U, "BSCL.Plataforma.BD::ACC.Usuario" U1
			WHERE U."IdUsuario" = p_out_new_user_id AND U1."IdUsuario" = :p_HeredarRolesUsuario;
        END IF;
		
		------------------------------------------
		--Genera Evento de Autorizacion
		------------------------------------------
		if v_RequiereAutorizacion='S' then
			declare v_out_idAuth 		bigint;
			declare v_out_AuthAdvertenciasContador 	int;
			declare v_out_AuthErroresContador 		int;
			v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
			CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
				p_IdEmpresa => p_IdEmpresa,
				p_IdUsuario => p_IdUsuario,
				p_IdModulo => p_IdModulo,
				p_UsaReglasNegocio => 'N',								
				p_IdTipoEvento => 'ENROLU',
				p_IdModuloSolicita => p_IdModulo,
				p_IdUsuarioSolicita => p_IdUsuario,
				p_Fecha => TO_VARCHAR (p_FechaIngreso, 'YYYY-MM-DD'), 
				p_IdConcepto =>  'ENROLU',
				p_NumeroOperacionConcepto =>  CAST (p_out_new_user_id AS NVARCHAR(40)),
				p_IdCliente => p_out_new_user_id,
				p_FechaOperacionConcepto => TO_VARCHAR (p_FechaIngreso, 'YYYY-MM-DD'),
				p_Glosa => p_CodigoUsuario,
				--salida
				p_out_IdEvento => v_out_idAuth,
				p_out_AdvertenciasContador => v_out_AuthAdvertenciasContador,
				p_out_AdvertenciasDetalle =>  v_out_AuthAdvertenciasDetalle,
				p_out_ErroresContador =>  v_out_AuthErroresContador,
				p_out_ErroresDetalle =>  v_out_AuthErroresDetalle);				
			--adjunta mensajes
			p_out_AdvertenciasDetalle =  select * from :v_out_AuthAdvertenciasDetalle UNION ALL
									     select * from :p_out_AdvertenciasDetalle;
			p_out_ErroresDetalle      =  select * from :v_out_AuthErroresDetalle UNION ALL
									     select * from :p_out_ErroresDetalle;
			--No se pudo crear 
			if 	p_out_ErroresContador > 0 then
				SIGNAL SALIDA_CONTROLADA;
			end if;								
			--Mensaje de Salida
			v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
			p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
									     select * from :p_out_AdvertenciasDetalle;
			SIGNAL SALIDA_CONTROLADA;
		end if;
		
	END;
END;