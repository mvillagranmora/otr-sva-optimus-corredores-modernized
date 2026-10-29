SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.ValidaSesionUsuario";
GO
CREATE PROCEDURE
-- 
--  Proposito: Valida una sesion de usuario 
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::ACC.ValidaSesionUsuario" (
	p_Token				NVARCHAR(520),
		
	-- Salida Correcta
	OUT p_out_IdEmpresa				BIGINT,	
	OUT p_out_IdUsuario				BIGINT,	
	OUT p_out_NombreEmpresa			NVARCHAR(520),
	OUT p_out_NombreUsuario			NVARCHAR(520),
    OUT p_out_CodigoMenuUsuario		NVARCHAR(100),
    OUT p_out_DebeCambiarClave		NVARCHAR(1),	
	OUT p_out_IdSocioDeNegocio		BIGINT,	
	OUT p_out_IdPersonaCorredora	BIGINT,	
	OUT p_out_IdMonedaSistema		NVARCHAR(3),	
	OUT p_out_IdPaisSistema			BIGINT,
	OUT p_out_FechaSistema			NVARCHAR(10),
	OUT p_out_BolsaDefectoSistema	NVARCHAR(3),
	OUT p_out_ValoresDefectoSistema	TABLE ("IdValorDefectoSistema"	NVARCHAR(40),
										   "Nombre"					NVARCHAR(255),
										   "Valor"					NVARCHAR(255),
										   "IdEmpresa"				NVARCHAR(40),
										   "IdEstado"				NVARCHAR(3)),
	OUT p_out_IdEmpleado			BIGINT,	
	OUT p_out_IdCliente				BIGINT,
	OUT p_out_IdPersona				BIGINT,
		 
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
		declare v_existe 						int;
		declare v_IdEmpresa						bigint;
		declare v_IdUsuario						bigint;
		DECLARE v_IdCanal 						nvarchar(3);
		declare v_FechaVigencia 				date;
		declare v_IdEstado						nvarchar(3);
		declare v_ZonaHoraria					nvarchar(520);	
		declare v_NombreUsuario					nvarchar(520);
		declare v_NombreEmpresa					nvarchar(520);
		declare v_CodigoMenuUsuario				nvarchar(100);
		declare v_DebeCambiarClave				nvarchar(1);
		declare v_IdSocioDeNegocio				bigint;
		declare v_IdPersonaCorredora						bigint;
		DECLARE p_sysDate NVARCHAR(10);
	
		DECLARE v_IdEntidad						BIGINT;

		
	
		--Valida que el token exista y sea valido
		select count(1) into v_existe 
		 from "BSCL.Plataforma.BD::ACC.Sesion"
		where "IdTokenSesion" = :p_Token and "TokenValido" = 'S';
		if v_existe = 0 then
			v_hash= select 'TOKEN' as "Codigo",:p_Token as "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('USUARIO_TOKEN_INVALIDO',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'USUARIO_TOKEN_INVALIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
									select * from :p_out_ErroresDetalle; 
			SIGNAL SALIDA_CONTROLADA; 		  			
		end if;		
		
		--obtiene id usuario
		select "IdUsuario", "IdCanal" 
		  into v_IdUsuario, v_IdCanal
		 from "BSCL.Plataforma.BD::ACC.Sesion"
		where "IdTokenSesion" = :p_Token and "TokenValido" = 'S';	
		
		--obtiene datos usuarios
		--AQUI
		select "IdEmpresa","FechaVigencia","IdEstado","CodigoUsuario", "IdEntidad", "CodigoMenu", "DebeCambiarClave"
		  into v_IdEmpresa,v_FechaVigencia, v_IdEstado,v_NombreUsuario, v_IdEntidad, v_CodigoMenuUsuario, v_DebeCambiarClave
		  from "BSCL.Plataforma.BD::ACC.Usuario" 
		where "IdUsuario" = :v_IdUsuario;	
	
		
		--obtiene datos empresa
		select emp."Nombre",emp."IdSocioDeNegocio",per."IdPersona" into v_NombreEmpresa ,v_IdSocioDeNegocio,v_IdPersonaCorredora
		from "BSCL.Plataforma.BD::PLA.Empresa" emp
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC on emp."IdSocioDeNegocio" = SOC."IdSocioDeNegocio"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = soc."IdPersona"
		where emp."IdEmpresa" = :v_IdEmpresa; 
								
		--obtiene parametros sistema
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:v_IdEmpresa, 'PLA.ZonaHoraria',:v_ZonaHoraria);	
				
		--obtiene moneda de sistema
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:v_IdEmpresa, 'PLA.MonedaSistema',p_out_IdMonedaSistema);	
		
		--obtiene país de sistema
		SELECT "IdPais" INTO p_out_IdPaisSistema FROM "BSCL.Plataforma.BD::PLA.Empresa" WHERE "IdEmpresa" = :v_IdEmpresa;	
		
		--obtiene fecha de sistema
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:v_IdEmpresa, 'PLA.FechaSistema',p_out_FechaSistema);	
		
		--obtiene bolsa defecto de sistema
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:v_IdEmpresa, 'PLA.BolsaDefectoSistema',p_out_BolsaDefectoSistema);	
		
		--obtiene valores defecto de sistema
		p_out_ValoresDefectoSistema =
			SELECT 
				"IdValorDefectoSistema",
				"Nombre",
				"Valor",
				"IdEmpresa",
				"IdEstado"
			FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarValorDefectoSistema"
			WHERE "IdEmpresa"	= :v_IdEmpresa
			  AND "IdEstado"	= 'ACT';
		
		--valida que Usuario no este bloqueado
		if :v_IdEstado = 'BLO' then
			v_hash= select 'NombreUsuario' as "Codigo",:v_NombreUsuario as "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('USUARIO_BLOQUEADO',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'USUARIO_BLOQUEADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
									select * from :p_out_ErroresDetalle; 
			SIGNAL SALIDA_CONTROLADA; 		  			
		end if;					
		
		CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (v_IdEmpresa, p_sysDate);
		--Valida la vigencia de la cuenta
		IF 	:v_FechaVigencia <> '0001-01-01' THEN
			if  :v_FechaVigencia < :p_sysDate then			
				v_hash= select 'NombreUsuario' as "Codigo",:v_NombreUsuario as "Valor" FROM DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('USUARIO_VIGENCIA_VENCIDA',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'USUARIO_VIGENCIA_VENCIDA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle; 
				SIGNAL SALIDA_CONTROLADA; 		  			
			end if;
		END IF;
		
		--Validar que el usuario sea un Empleado o Cliente
		IF :v_IdCanal = 'CLI' THEN
			select
				COALESCE(CE."IdEmpleado",0) AS "IdEmpleado",
				COALESCE(CC."IdCliente", 0) AS "IdCliente",
				COALESCE(PP."IdPersona", 0) AS "IdPersona"			
				INTO p_out_IdEmpleado, p_out_IdCliente, p_out_IdPersona
			from "BSCL.Plataforma.BD::ACC.Usuario" U
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = U."IdEntidad" AND CE."IdEstado" = 'ACT' AND U."TipoEntidad" = 'EMP'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"  CC ON CC."IdCliente" = U."IdEntidad" AND CC."IdEstado" IN ('ACT', 'BLO') AND U."TipoEntidad" = 'CLI'
			LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"  PP ON PP."IdPersona" = U."IdEntidad" AND PP."IdEstado" = 'ACT'AND U."TipoEntidad" = 'PER'
			WHERE U."IdEntidad" = v_IdEntidad and U."IdUsuario" = v_IdUsuario;
		ELSE
			select
					COALESCE(CE."IdEmpleado",0) AS "IdEmpleado",
					(0) AS "IdCliente",
					COALESCE(PP."IdPersona", 0) AS "IdPersona"			
					INTO p_out_IdEmpleado, p_out_IdCliente, p_out_IdPersona
				from "BSCL.Plataforma.BD::ACC.Usuario" U
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE ON CE."IdEmpleado" = U."IdEntidad" AND CE."IdEstado" = 'ACT' AND U."TipoEntidad" = 'EMP'
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"  PP ON PP."IdPersona" = U."IdEntidad" AND PP."IdEstado" = 'ACT'AND U."TipoEntidad" = 'PER'
				WHERE U."IdEntidad" = v_IdEntidad and U."IdUsuario" = v_IdUsuario;
		END IF;
		
		IF :p_out_IdEmpleado=0 AND :p_out_IdCliente=0 AND  :p_out_IdPersona=0 THEN
			v_hash= select 'NombreUsuario' as "Codigo",:v_NombreUsuario as "Valor" FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('USUARIO_NO_VALIDO',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'USUARIO_NO_VALIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
									select * from :p_out_ErroresDetalle; 
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
						
		--Salida	
		p_out_IdEmpresa 	 	 := 	:v_IdEmpresa;	
		p_out_NombreEmpresa 	 := 	:v_NombreEmpresa;			    
		p_out_IdUsuario		 	 := 	:v_IdUsuario;
		p_out_NombreUsuario  	 :=		:v_NombreUsuario;
		p_out_CodigoMenuUsuario  :=		:v_CodigoMenuUsuario;
		p_out_DebeCambiarClave   :=		:v_DebeCambiarClave;
		p_out_IdSocioDeNegocio   :=		:v_IdSocioDeNegocio;   
		p_out_IdPersonaCorredora :=		:v_IdPersonaCorredora;	
		--------------------
		--FIN SP 
		--------------------
	END;	
END;