SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.CrearUsuarioInterno";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.CrearUsuarioInterno" (
	IN  p_CodigoUsuario                     NVARCHAR(100),
	IN  p_Tipo                     		NVARCHAR(100)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS


BEGIN

	-- @Bolsa123456 / A51363D9BBB4F9238EC13B8BEA575DFB971D66C36CEA91028288A201C3214944
	-- 1 / E0BC614E4FD035A488619799853B075143DEEA596C477B8DC077E309C0FE42E9
	-- 123 / 173AF653133D964EDFC16CAFE0ABA33C8F500A07F3BA3F81943916910C257705

	if (:p_Tipo = 'SEGURIDAD') THEN

	    INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario"
		("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
		VALUES("BSCL.Plataforma.BD.Secuencias::Usuario".NEXTVAL , 1, :p_CodigoUsuario, 'A51363D9BBB4F9238EC13B8BEA575DFB971D66C36CEA91028288A201C3214944', '0001-01-01', '0001-01-01', '2023-05-12', 999999.00000000, 'S', '18:34:59', '', 'ACT', 0, '0001-01-01 00:00:00.000', 'EMP', (SELECT "IdEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado" bsnbee WHERE "CodigoEmpleado" = 'SISTEMA'), 'S', 'N', '2022-01-17 19:22:14.000', '', 9999, 99999, '', 0, '0001-01-01 00:00:00.000', 'N', 'S', 'S', 'S', 'S');
		
		INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario", "IdUsuario", "IdRol", "EsAutorizado")
		SELECT	"BSCL.Plataforma.BD.Secuencias::RolUsuario".NEXTVAL AS "IdRolUsuario"
				,"IdUsuario"
				,(SELECT "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" = 'SEGURIDAD') AS "IdRol"
				, 'S' AS "EsAutorizado"
		FROM "BSCL.Plataforma.BD::ACC.Usuario" usu
		WHERE "CodigoUsuario" = :p_CodigoUsuario;

	END IF;
	
	if (:p_Tipo = 'SOPORTE') THEN

	    INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario"
		("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
		VALUES("BSCL.Plataforma.BD.Secuencias::Usuario".NEXTVAL , 1, :p_CodigoUsuario, 'A51363D9BBB4F9238EC13B8BEA575DFB971D66C36CEA91028288A201C3214944', '0001-01-01', '0001-01-01', '2023-05-12', 999999.00000000, 'S', '18:34:59', '', 'ACT', 0, '0001-01-01 00:00:00.000', 'EMP', (SELECT "IdEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado" bsnbee WHERE "CodigoEmpleado" = 'SISTEMA'), 'S', 'N', '2022-01-17 19:22:14.000', '', 9999, 99999, '', 0, '0001-01-01 00:00:00.000', 'N', 'S', 'S', 'S', 'S');
		
		INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario", "IdUsuario", "IdRol", "EsAutorizado")
		SELECT	"BSCL.Plataforma.BD.Secuencias::RolUsuario".NEXTVAL AS "IdRolUsuario"
				,"IdUsuario"
				,(SELECT "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" = 'SOPORTE_BSX') AS "IdRol"
				, 'S' AS "EsAutorizado"
		FROM "BSCL.Plataforma.BD::ACC.Usuario" usu
		WHERE "CodigoUsuario" = :p_CodigoUsuario;

	END IF;

END;

--set schema bscl;
--CALL "BSCL.Plataforma.BD.Servicios::ACC.CrearUsuarioInterno"('FHEVIA','SOPORTE');
--CALL "BSCL.Plataforma.BD.Servicios::ACC.CrearUsuarioInterno"('EROA_EXT','SOPORTE');
--CALL "BSCL.Plataforma.BD.Servicios::ACC.CrearUsuarioInterno"('DGAMONAL_EXT','SOPORTE');
--COMMIT;