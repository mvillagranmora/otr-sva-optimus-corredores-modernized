SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin";

CREATE PROCEDURE 
-- 
--  Proposito: Evaluar si usuario es tiene rol ADMIN
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 17/06/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.ConsultarUsuarioAdmin" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	-- Parametros entrada 
	OUT p_out_EsAdmin						NVARCHAR(1)		--'S' o 'N'
) 

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	declare v_roles_usuario table("IdRol" bigint);
	declare v_cantidad_roles_admin bigint;
	DECLARE IdRolAdmin NVARCHAR(520);
	DECLARE ERROR_CLIENTE_INVALIDO CONDITION FOR SQL_ERROR_CODE 10012;	
	
	BEGIN
	
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL ERROR_CLIENTE_INVALIDO;
		END;
		
		
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion" (:p_IdEmpresa, 'ACC.IdRolAdmin',:IdRolAdmin);	

		v_roles_usuario = select "IdRol" from
		"BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesUsuario" 
		where "EsAutorizado" = 'S' AND "IdEmpresa" = :p_IdEmpresa AND "IdUsuario" = :p_IdUsuario;
		
		SELECT count("IdRol") into v_cantidad_roles_admin
		FROM :v_roles_usuario
		WHERE "IdRol" = TO_BIGINT(:IdRolAdmin); -- 1 es el id en duro del rol ADMIN

		IF :v_cantidad_roles_admin > 0 THEN
			p_out_EsAdmin := 'S';
		ELSE
			p_out_EsAdmin := 'N';
		END IF;
		
	END;				    
END;