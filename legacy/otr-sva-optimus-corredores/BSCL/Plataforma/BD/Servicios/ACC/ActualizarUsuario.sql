SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.ActualizarUsuario";
-- 
--  Proposito: Servicio para la Actualizacion datos de un usuario.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 08/02/2016
--  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.ActualizarUsuario" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametro de entrada que almacenara los accesos del perfil
	IN  p_CodigoUsuario				nvarchar(40),  		
	IN  p_Clave						nvarchar(520), 
	IN  p_HeredarRolesUsuario       bigint,
	IN  p_FechaVigencia				date,
	IN	p_IdEstado					nvarchar(3),
	--parametros configuracion usuario
	IN	p_UsuarioCorredores			nvarchar(1),
	IN	p_UsuarioNegocios			nvarchar(1),
	IN	p_UsuarioClientes			nvarchar(1),
	IN	p_UsuarioServicios			nvarchar(1),

	-- Parametros salida del servicio publico
	OUT p_out_update				 NVARCHAR(5),
	
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
	DECLARE v_ClaveAnt nvarchar(520); 
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
			--Error interno no manejado  (El log sera¡ captado por la capa de XS control)		
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
		DECLARE p_DetalleLog 		TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE p_sysDate 			NVARCHAR(20);
		DECLARE v_IdUsuario 		BIGINT;
		DECLARE v_Clave				nvarchar(520);
		DECLARE v_FechaVigencia		date;
		DECLARE v_DebeCambiarClave	NVARCHAR(1) DEFAULT 'S';
		DECLARE v_EsAutorizado      NVARCHAR(1) DEFAULT 'N';
		DECLARE v_UltimoCambioClave	seconddate;
		DECLARE v_IdEstado			nvarchar(3);
		DECLARE v_CountHeredarRolesUsuario          BIGINT;
		DECLARE v_count BIGINT;
		
		--------------------
		--Logica Aqui
		--------------------
		select "IdUsuario", "Clave", "FechaVigencia", "UltimoCambioClave", "IdEstado", "EsAutorizado" into v_IdUsuario, v_Clave, v_FechaVigencia, v_UltimoCambioClave, v_IdEstado, v_EsAutorizado
		FROM "BSCL.Plataforma.BD::ACC.Usuario"
		WHERE "CodigoUsuario" = upper(p_CodigoUsuario) AND "IdEmpresa" = p_IdEmpresa; 
		v_ClaveAnt = v_Clave;
		--Logear nombre de usuario
		p_DetalleLog =  SELECT upper(p_CodigoUsuario) as "SubRegistro", 'FechaVigencia' as "CampoRegistro", TO_VARCHAR(v_FechaVigencia) as "ValorAntiguo", TO_VARCHAR(p_FechaVigencia) as "ValorNuevo" FROM dummy
		UNION ALL SELECT upper(p_CodigoUsuario) as "SubRegistro", 'IdEstado' as "CampoRegistro", v_IdEstado as "ValorAntiguo", p_IdEstado as "ValorNuevo" FROM dummy;
	
		IF p_Clave <> '' THEN
			--Viene clave en el servicio, actualizar.
			SELECT HASH_SHA256(to_binary(p_Clave)) "CLAVE" INTO p_Clave FROM dummy;
			
			-- VERIFICAR QUE NO SE ENCUENTRE LA CLAVE EN LA TABLA LOG
			
				SELECT COUNT(*) into v_count FROM 
				(SELECT "Clave", "UltimoCambioDeClave" from "BSCL.Plataforma.BD::ACC.LogUsuarioClave" 
				WHERE "IdUsuario" = v_IdUsuario order by "UltimoCambioDeClave" desc limit 10) LogUsuario
				where "Clave" = p_Clave; 

			   		if (p_Clave = v_Clave or v_count > 0) THEN
							--LAS CLAVES SON IGUALES, ERROR
							v_hash= select 'Clave' as "Codigo", '*****' as "Valor" FROM DUMMY;
							CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_CLAVES_IGUALES',:v_hash,:v_erroroutmsg);
							p_out_ErroresDetalle =  select 'UI_CLAVES_IGUALES' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
													select * from :p_out_ErroresDetalle;
							SIGNAL SALIDA_CONTROLADA;
				
					ELSE
						--ACTUALIZAR CLAVE
						v_Clave := p_Clave;
						v_DebeCambiarClave := 'S';
						SELECT CURRENT_UTCTIMESTAMP INTO v_UltimoCambioClave FROM DUMMY WHERE 1=1;
					
						p_DetalleLog =  select upper(p_CodigoUsuario) as "SubRegistro", 'Clave' as "CampoRegistro", '*****' as "ValorAntiguo", '*****' as "ValorNuevo"
						FROM dummy
						UNION ALL SELECT "SubRegistro", "CampoRegistro", "ValorAntiguo", "ValorNuevo" FROM :p_DetalleLog;
					END IF;
				--END IF;
        ELSE
            IF v_EsAutorizado = 'S' THEN
                v_DebeCambiarClave:='N';
            END IF;
		END IF;
		
		
		
		
		UPDATE "BSCL.Plataforma.BD::ACC.Usuario" 
		SET "FechaVigencia" = p_FechaVigencia, "IdEstado" = p_IdEstado, "Clave" = v_Clave, 
			"DebeCambiarClave" = v_DebeCambiarClave, "UltimoCambioClave" = v_UltimoCambioClave,
			"UsuarioCorredores" = p_UsuarioCorredores, "UsuarioNegocios" = p_UsuarioNegocios,
			"UsuarioClientes" = p_UsuarioClientes, "UsuarioServicios" = p_UsuarioServicios
		WHERE "IdUsuario" = v_IdUsuario;
	
	
		-- Se cambia el indicador que permite el acceso a optimus cliente check OWP
			
		UPDATE ind 
		SET ind."ValorIndicador" = CASE WHEN p_UsuarioClientes = 'S' THEN 'N' ELSE 'S' END
		FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" usu
		INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli
			ON usu."IdPersona" = cli."IdPersona" 
		INNER JOIN "BSCL.Plataforma.BD::IND.Indicador" ind
			ON cli."IdCliente" = ind."IdEntidad" 
		INNER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" tipo
			ON ind."IdTipoIndicador" = tipo."IdTipoIndicador" 
		WHERE "CodigoTipoIndicador" = 'NPA'
		AND "IdUsuario" = v_IdUsuario;
        
        -- Fin cambio check OWP
        		
		-- Guardar historial de claves
		INSERT INTO "BSCL.Plataforma.BD::ACC.LogUsuarioClave"("IdLogUsuario","IdUsuario", "Clave", "UltimoCambioDeClave") 
		VALUES("BSCL.Plataforma.BD.Secuencias::ACC.IdLogUsuario".NEXTVAL,v_IdUsuario,v_ClaveAnt,v_UltimoCambioClave);

        --Validar que se quiera heredarRoles para el usuario
        SELECT COUNT(1) INTO v_CountHeredarRolesUsuario FROM "BSCL.Plataforma.BD::ACC.Usuario"
        WHERE "IdUsuario" = :p_HeredarRolesUsuario AND "IdEmpresa" = :p_IdEmpresa;
                        
        IF v_CountHeredarRolesUsuario = 0 AND :p_HeredarRolesUsuario <> -1 THEN
            v_hash= select 'SOCIO' as "Codigo", '' as "Valor" FROM DUMMY;
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('UI_HEREDAR_ROLES_ERROR',:v_hash,:v_erroroutmsg);
            p_out_ErroresDetalle =  select 'UI_HEREDAR_ROLES_ERROR' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
                                    select * from :p_out_ErroresDetalle;
        END IF;
        
        IF  v_CountHeredarRolesUsuario > 0 AND :p_HeredarRolesUsuario <> -1 THEN
            --Eliminar Roles anteriores
            DELETE FROM "BSCL.Plataforma.BD::ACC.RolUsuario" WHERE "IdUsuario" = :v_IdUsuario;
            
            --Ingresar nuevos roles.
            INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario"("IdRolUsuario", "IdUsuario", "IdRol", "EsAutorizado")
            SELECT 
	            "BSCL.Plataforma.BD.Secuencias::RolUsuario".NEXTVAL AS "IdRolUsuario",
	            v_IdUsuario AS "IdUsuario",
	            RU."IdRol",
	            'S' AS "EsAutorizado"
            FROM  "BSCL.Plataforma.BD::ACC.RolUsuario" RU
            WHERE RU."IdUsuario" = :p_HeredarRolesUsuario;
            
            --HEREDAR MENU DE USUARIO
            UPDATE U 
            SET U."CodigoMenu" = U1."CodigoMenu"
            FROM "BSCL.Plataforma.BD::ACC.Usuario" U, "BSCL.Plataforma.BD::ACC.Usuario" U1
            WHERE U."IdUsuario" = v_IdUsuario AND U1."IdUsuario" = :p_HeredarRolesUsuario;
            
        END IF;
		
		--Ejecutar el programa de logeo.
		SELECT CURRENT_DATE into p_sysDate from DUMMY;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
		p_IdTipoLog => 'ACTUALIZACION' 
		,p_IdModulo => 'PLA' 
		,p_IdUsuario => p_IdUsuario 
		,p_IdEmpresa => p_IdEmpresa 
		,p_FechaSistemaLog => TO_VARCHAR (p_sysDate, 'YYYY-MM-DD')
		,p_IdUsuarioAtendido => p_IdUsuario
		,p_IdTipoRegistro => 'USUARIO'
		,p_DetalleLog => :p_DetalleLog
		);

		p_out_update := 'ok++';
		
	END;
END;