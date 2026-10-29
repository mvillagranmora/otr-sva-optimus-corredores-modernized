SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.EliminarPerfilRol";
-- 
--  Proposito: Servicio para la eliminnacion de perfiles asignados a roles.
--  Autor: - CHERNANDEZ
--  Fecha Creacion: 26/01/2016
--  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.EliminarPerfilRol" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para la eliminacion del rol
	IN  p_IdRolPerfil				bigint,  		

	-- Parametros salida del servicio publico
	OUT p_flagRolPerfil				bigint,
		
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
		DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		DECLARE p_sysDate NVARCHAR(20);
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			DECLARE v_countRolesPerfil bigint;
			
			select count ("IdRolPerfil") into v_countRolesPerfil from "BSCL.Plataforma.BD::ACC.RolPerfil" where "IdRolPerfil" = p_IdRolPerfil;
			IF v_countRolesPerfil < 1 THEN			
			p_out_ErroresDetalle =  select 'ROL_NO_TIENE_PERFIL' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
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
		SELECT CURRENT_DATE into p_sysDate from DUMMY;
		
		p_DetalleLog =  (select U."CodigoUsuario" as "SubRegistro", 'Perfil'  as "CampoRegistro", P."Nombre" as "ValorAntiguo", '' as "ValorNuevo"
		from "BSCL.Plataforma.BD::ACC.Usuario" as U 
		inner join "BSCL.Plataforma.BD::ACC.RolUsuario" as RU on U."IdUsuario" = RU."IdUsuario"
		inner join "BSCL.Plataforma.BD::ACC.Rol" as R on RU."IdRol" = R."IdRol"
		inner join "BSCL.Plataforma.BD::ACC.RolPerfil" as RP on Rp."IdRol" = R."IdRol" and RP."IdRolPerfil" = p_IdRolPerfil
		inner join "BSCL.Plataforma.BD::ACC.Perfil" as P on RP."IdPerfil" = P."IdPerfil");
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
		p_IdTipoLog => 'ELIMINACION' 
		,p_IdModulo => 'PLA' 
		,p_IdUsuario => p_IdUsuario 
		,p_IdEmpresa => p_IdEmpresa 
		,p_FechaSistemaLog => TO_VARCHAR (p_sysDate, 'YYYY-MM-DD')
		,p_IdUsuarioAtendido => p_IdUsuario
		,p_IdTipoRegistro => 'ROL'
		,p_DetalleLog => :p_DetalleLog
		);
		
		delete from "BSCL.Plataforma.BD::ACC.RolPerfil" where "IdRolPerfil" = p_IdRolPerfil;

		
	END;
END;