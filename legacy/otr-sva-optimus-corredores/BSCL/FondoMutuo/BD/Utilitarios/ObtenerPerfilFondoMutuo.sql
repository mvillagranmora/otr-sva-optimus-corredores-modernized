SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::ObtenerPerfilFondoMutuo";

CREATE PROCEDURE
-- 
--  Proposito: Obtiene el tipo de perfil de fondo 
--  Autor: - Gat - Blac
--  Fecha Creacion: 08/07/2016 
--     
"BSCL.FondoMutuo.BD.Utilitarios::ObtenerPerfilFondoMutuo" (
 	IN 	p_IdUsuario				BIGINT,
 	OUT	p_out_PerfilFondoMutuo 	NVARCHAR(40)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL 
	READS SQL DATA AS
BEGIN
	DECLARE v_count_perfiles BIGINT;
	
	p_out_PerfilFondoMutuo := '';
	
	SELECT COUNT(1) into v_count_perfiles
	FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "Configuracion"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario" AS "Perfiles" ON "Perfiles"."IdPerfil" = "Configuracion"."Value"
	WHERE "Key" IN ('PERFIL_COMERCIAL', 'PERFIL_BACKOFFICE') AND "Perfiles"."IdUsuario" = p_IdUsuario;
	
	IF :v_count_perfiles > 0 THEN
	
		SELECT TOP 1 "Key" INTO p_out_PerfilFondoMutuo
		FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" AS "Configuracion"
		INNER JOIN "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario" AS "Perfiles" ON "Perfiles"."IdPerfil" = "Configuracion"."Value"
		WHERE "Key" IN ('PERFIL_COMERCIAL', 'PERFIL_BACKOFFICE') AND "Perfiles"."IdUsuario" = p_IdUsuario;
	
	END IF;
END;
