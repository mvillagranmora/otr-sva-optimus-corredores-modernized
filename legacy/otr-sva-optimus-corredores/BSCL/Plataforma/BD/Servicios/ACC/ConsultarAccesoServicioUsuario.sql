SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::ACC.ConsultarAccesoServicioUsuario";

CREATE PROCEDURE 
-- 
--  Proposito: Evaluar si el usuario tiene acceso a ejecutar un servicio
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 17/06/2016
--  
"BSCL.Plataforma.BD.Servicios::ACC.ConsultarAccesoServicioUsuario" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	bigint,			--ID Empresa destino
	IN  p_IdUsuario						 	bigint,			--ID del usuario que solicita el servicio
	-- Parametros entrada 
	IN	p_IdServicio						bigint,
	
	OUT p_out_TieneAcceso					NVARCHAR(1)		--'S' o 'N'
) 

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	DECLARE v_servicios_usuario table("IdServicio" bigint);
	DECLARE v_cantidad_accesos bigint;
	
	DECLARE ERROR_CLIENTE_INVALIDO CONDITION FOR SQL_ERROR_CODE 10012;	
	BEGIN
	
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			SIGNAL ERROR_CLIENTE_INVALIDO;
		END;
		
		v_servicios_usuario = select "IdServicio" from "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso" 
		where "IdUsuario" = :p_IdUsuario and "Existe"='S' and "IdServicio" > 0
		UNION ALL --servicios globales.
		SELECT "IdServicio" FROM "BSCL.Plataforma.BD::ACC.Acceso" where "TipoAcceso" = 'SERV';
		
		select COUNT("IdServicio") into v_cantidad_accesos
		FROM :v_servicios_usuario
		WHERE "IdServicio" = :p_IdServicio;
		
		if :v_cantidad_accesos > 0 THEN
			p_out_TieneAcceso := 'S';
		ELSE
			p_out_TieneAcceso := 'N';
		END IF;
		
	END;				    
END;
