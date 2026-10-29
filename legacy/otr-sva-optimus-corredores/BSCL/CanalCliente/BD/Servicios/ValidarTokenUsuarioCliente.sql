SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.CanalCliente.BD.Servicios::ValidarTokenUsuarioCliente";

CREATE PROCEDURE 
-- 
--  Proposito: Valida que el token sea valido.
--  Autor: - BCS
--  Fecha Creacion: 19/12/2016
--  
"BSCL.CanalCliente.BD.Servicios::ValidarTokenUsuarioCliente" (
	IN p_Token						NVARCHAR(520),
	-- Salida Info sesion
	OUT p_out_IdEmpresa				BIGINT,	
	OUT p_out_IdUsuario				BIGINT,	 
	OUT p_out_NombreUsuario			NVARCHAR(80), 
	OUT p_out_TipoEntidad			NVARCHAR(3), 
	OUT p_out_IdEntidad				BIGINT,
	-- Retorno
	OUT p_out_TokenValido			NVARCHAR(1), 
	OUT p_out_Mensaje				NVARCHAR(80) 
	
 ) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	--------------------
	--Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
    	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		p_out_TokenValido := 'N';
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_TokenValido := 'N';
		IF ::SQL_ERROR_CODE > 10000 THEN
			p_out_Mensaje := 'Error de aplicativo '||::SQL_ERROR_CODE;												
		ELSE
			p_out_Mensaje := 'Error interno '||::SQL_ERROR_CODE;			
			RESIGNAL; 
		END IF;		
	END;	
	
	
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------	
		declare v_existe 						int;
		declare v_IdEstado						nvarchar(3);		
		declare v_FechaVigencia 				date;
						
		--Valida que el token exista y sea valido
		select count(1) into v_existe 
		 from "BSCL.Plataforma.BD::ACC.Sesion"
		where "IdTokenSesion" = :p_Token and "TokenValido" = 'S';
		if v_existe = 0 then
			p_out_Mensaje := 'Token invalido';			
			SIGNAL SALIDA_CONTROLADA; 		  			
		end if;		
		
		--obtiene id usuario
		select "IdUsuario" into p_out_IdUsuario
		 from "BSCL.Plataforma.BD::ACC.Sesion"
		where "IdTokenSesion" = :p_Token and "TokenValido" = 'S';	
		
		--obtiene datos usuarios
		select COUNT("IdEmpresa") INTO v_existe
		from "BSCL.Plataforma.BD::ACC.Usuario" 
		where "IdUsuario" = :p_out_IdUsuario AND "TipoEntidad" = 'CLI';
		
		IF v_existe > 0 THEN
			select "IdEmpresa","FechaVigencia","IdEstado","CodigoUsuario", "TipoEntidad", "IdEntidad" into p_out_IdEmpresa,v_FechaVigencia, v_IdEstado,p_out_NombreUsuario, p_out_TipoEntidad, p_out_IdEntidad
			from "BSCL.Plataforma.BD::ACC.Usuario" 
			where "IdUsuario" = :p_out_IdUsuario AND "TipoEntidad" = 'CLI';	
														
			--valida que Usuario no este bloqueado
			if :v_IdEstado = 'BLO' OR CURRENT_UTCTIMESTAMP > :v_FechaVigencia then
				p_out_Mensaje := 'Cuenta bloqueada o vencida';	
				SIGNAL SALIDA_CONTROLADA; 		  			
			end if;
			
			IF :p_out_IdEmpresa > 0 AND :p_out_NombreUsuario <> '' THEN
				p_out_TokenValido := 'S';
				p_out_Mensaje := 'OK++';
			END IF;
		ELSE
			p_out_Mensaje := 'Usuario no es cliente';
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
