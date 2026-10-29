SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.CrearUsuario";
-- 
--  Proposito: Servicio para la creacion de usuarios.
--  Autor: - CHERNANDEZ
--  Fecha Creacion: 14/01/2016
--  
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::ACC.CrearUsuario" (
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS

/*	crear Persona "SISTEMA OPTIMUS" ||||| Empleado con Codigo Empleado "SIS" a traves del sistema y usuario  ||||| 
para funcionamiento de job del sistema */

--	SISTEMA    Primero crear empleado SIS
BEGIN
	DECLARE v_IdPersona INT;
	DECLARE v_IdEmpleado INT;
	DECLARE v_IdUsuario INT := 0;
	DECLARE v_Contador INT;
	DECLARE v_IdRol INT;
	DECLARE v_IdDocumentoIdentidad INT;
	DECLARE v_base INT := 7000;
	DECLARE v_rut NVARCHAR(11);
	DECLARE p_idEmpresa BIGINT := 1;

	SELECT "BSCL"."BSCL.SocioNegocio.BD.Secuencias::Persona".NEXTVAL INTO v_IdPersona FROM DUMMY;	
	SELECT count(*) INTO v_Contador FROM "BSCL.SocioNegocio.BD::EMP.Empleado" WHERE "CodigoEmpleado" = 'SISTEMA'; --*jala* dejo el codigo SISTEMA pues la rutina de creacion de empleados crea un codigo con las iniciales del nombre y coincidio en crear SIS por Sandra Inostroza Silva (por ejemplo)
	SELECT "Nombre" AS "Salida" FROM "BSCL.Plataforma.BD::PLA.Empresa";
	
	IF v_Contador = 0 THEN 
		INSERT INTO "BSCL.SocioNegocio.BD::PER.Persona" 
			(
				"IdPersona",
				"IdEstado",
				"IdDocumentoIdentidad",
				"Tipo",
				"Nombres",
				"ApellidoPaterno",
				"ApellidoMaterno",
				"RazonSocial",
				"IdGiroPrincipal",
				"IdPaisNacimiento",
				"IdPaisResidencia",
				"FechaNacimiento",
				"FechaDefuncion",
				"Sexo",
				"EsExtranjero",
				"EstadoCivil",
				"RegimenConyugal",
				"Cargo",
				"Empleador",
				"IdDireccionFisicaPrincipal",
				"IdDireccionElectronicaPrincipal",
				"IdTelefonoPrincipal",
				"IdProfesion",
				"FotoPersona",
				"FotoFirmaPersona",
				"FechaInicioActividad",
				"FechaIngreso",
				"FechaCreacion",
				"FechaModificacion",
				"EsEnrolado",
				"IdCarga",
				"Alias",
				"IdOrigen",
				"Borrado",
				"IdUsuarioCreador",
				"IdPersonaConyuge",
				"IdPersonaRepresentante",
				"IdTipoBloqueo",
				"FechaBloqueo",
				"ComentarioBloqueo",
				"IdTipoDesbloqueo",
				"FechaDesbloqueo",
				"ComentarioDesbloqueo",
				"IdPaisNacionalidadPrincipal",
				"GlobalLegalEntity",
				"IdPersonaEmpleador",
				"CodigoTipoCargoEmpleado",
				"FechaInicioConyuge",
				"FechaInicioRepresentante",
				"DetalleCargo",
				"IdNombreFantasiaPrincipal",
				"IdRangoPatrimonio",
				"IdGradoInstruccion",
				"AgnosServicio",
				"IngresoPromedioMensual",
				"IdDocumentoIdentidadAdicional",
				"IdPaisResidenciaSecundaria",
				"IdPaisNacionalidadSecundaria",
				"IdGiroCIIU")
			VALUES(
			v_IdPersona,
			'ACT',
			0,
			'N',
			'SISTEMA',
			'OPTIMUS',
			'SAP',
			'',
			0,
			44,
			44,
			'0001-01-01',
			'0001-01-01',
			'M',
			'N',
			'SO',
			NULL,
			'',
			'',
			Null,
			Null,
			NULL,
			Null,
			'',
			'',
			'0001-01-01 00:00:00',
			'0001-01-01 00:00:00',
			'0001-01-01 00:00:00',
			'0001-01-01 00:00:00',
			'N',
			0,
			'',
			0,
			'N',
			0,
			0,
			0,
			'',
			'0001-01-01 00:00:00',
			'', 
			'', 
			'0001-01-01 00:00:00',
			'',
			0,
			'',
			0,
			'',
			'0001-01-01 00:00:00',
			'0001-01-01 00:00:00',
			'',
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0,
			0);
			
			--verifica si existe el RUT 0-0 para asignarle a la nueva persona, si no crea ese RUT			

			SELECT count(*) INTO v_Contador FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" WHERE "DocumentoIdentidad" = '0-0';
			
			--si no se hace esto el proceso se cae por no encontrar datos
			IF v_Contador > 0 THEN
			
				SELECT "IdDocumentoIdentidad" INTO v_IdDocumentoIdentidad FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" WHERE "DocumentoIdentidad" = '0-0';
			
			END IF;
			
			IF v_IdDocumentoIdentidad IS NULL THEN
			
					SELECT "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad".NEXTVAL INTO v_IdDocumentoIdentidad FROM DUMMY;
			
					INSERT INTO "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" 
							(
							"IdDocumentoIdentidad",
							"IdPersona",
							"IdTipoDocumentoIdentidad",
							"DocumentoIdentidad",
							"NumeroSerie",
							"FechaEmision",
							"FechaVencimiento",
							"IdEstado",
							"FechaIngreso",
							"FechaCreacion",
							"FechaModificacion",
							"IdCarga",
							"IdEstadoAutorizacion",
							"IdOrigen", 
							"Borrado",
							"IdUsuarioCreador",
							"Predeterminado",
							"IdTipoDocumentoIdentidadAsociado",
							"TipoPersona",
							"DocumentoIdentidadAsociado") 
							VALUES(
							v_IdDocumentoIdentidad, 
							v_IdPersona,
							'R',
							'0-0',
							'12345',
							'0001-01-01', 
							'0001-01-01',
							'ACT',
							'0001-01-01 00:00:00', 
							'0001-01-01 00:00:00', 
							'0001-01-01 00:00:00', 
							0, 
							'',
							0,
							'N', 
							0,
							'N',
							'', 
							'', 
							'');
							
			END IF;
			
			UPDATE "BSCL.SocioNegocio.BD::PER.Persona" 
			SET "IdDocumentoIdentidad" = v_IdDocumentoIdentidad
			WHERE "IdPersona" = v_IdPersona;
			
			--crea empleado
			SELECT "BSCL"."BSCL.SocioNegocio.BD.Secuencias::Empleado".NEXTVAL INTO v_IdEmpleado FROM DUMMY;	

			INSERT INTO "BSCL.SocioNegocio.BD::EMP.Empleado"
						(
						"IdEmpleado",
						"IdPersona",
						"CodigoEmpleado",
						"IdEmpresa",
						"IdEmpleadoSupervisor",
						"IdSucursal",
						"FechaInicioCargo",
						"FechaTerminoCargo",
						"FechaIngreso",
						"FechaCreacion",
						"FechaModificacion",
						"IdCarga",
						"IdEstado",
						"CodigoAgenteMigracion",
						"CodigoSupervisorAgenteMigracion",
						"Borrado",
						"EsEnrolado",
						"IdUsuarioCreador",
						"UtilizaPlantillaEmpleado",
						"IdPlantillaEmpleado",
						"CodigoTipoCargoEmpleado",
						"IdTipoBloqueo",
						"FechaBloqueo",
						"ComentarioBloqueo",
						"IdTipoDesbloqueo",
						"FechaDesbloqueo",
						"ComentarioDesbloqueo",
						"DetalleCargo",
						"IdDireccionElectronica",
						"CargoIndefinido",
						"RestringeCarteraCliente",
						"RestringeOrdenCliente")
						VALUES (
						 v_IdEmpleado,
						 v_IdPersona,
						 'SISTEMA',
						 p_IdEmpresa,
						 0,
						 0,
						 '0001-01-01',
						 '0001-01-01',
						 '0001-01-01 00:00:00',
						 '0001-01-01 00:00:00',
						 '0001-01-01 00:00:00',
						 0,
						 'ACT',
						 '',
						 '',
						 'N',
						 'S',
						 0,
						 'N',
						 NULL,
						 '',
						 '',
						 '0001-01-01 00:00:00',
						 '',
						 '',
						 '0001-01-01 00:00:00',
						 '',
						 '',
						 0,
						 '',
						 'S',
						 'S');
						 
			INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
			("IdSocioDeNegocio", "IdPersona", "IdEmpresa", "IdCliente", "IdEmpleado", "IdOperador", "IdProveedor", "IdContraparte", "Borrado", "IdCarga", "IdOrigen")
			SELECT "BSCL.SocioNegocio.BD.Secuencias::SocioDeNegocio".NEXTVAL AS "IdSocioDeNegocio",  
			v_IdPersona AS "IdPersona", p_IdEmpresa AS "IdEmpresa", NULL, v_IdEmpleado AS "IdEmpleado", null, null, null, 'N', 9999, 9999
			FROM DUMMY;
						 
			SELECT count(*) INTO v_Contador FROM "BSCL.Plataforma.BD::ACC.Usuario" WHERE "CodigoUsuario" = 'SISTEMA';
	
			--Verifica que no exista un usuario con codigo "SISTEMA"
			IF v_Contador = 0 THEN 
						 
				
				INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" (
				    "IdUsuario","IdEmpresa","CodigoUsuario","Clave","FechaIngreso","FechaVigencia","FechaUltimaConexion","DiasVencimientoClave",
				    "Conectado","Hora","Terminal","IdEstado","IntentosConexion","FechaBloqueoTemporal","TipoEntidad","IdEntidad","EsAutorizado",
				    "DebeCambiarClave","UltimoCambioClave","CodigoMenu","IdCarga","IdOrigen","UsuarioClientes","UsuarioNegocios","UsuarioServicios","UsuarioCorredores","EsUsuarioBolsa")
				SELECT 
				    v_IdUsuario AS "IdUsuario", p_IdEmpresa AS "IdEmpresa", 'SISTEMA' AS "CodigoUsuario",
				    'E0BC614E4FD035A488619799853B075143DEEA596C477B8DC077E309C0FE42E9' AS "Clave",'0001-01-01' AS "FechaIngreso",
				    '2999-12-31' AS "FechaVigencia",'0001-01-01' AS "FechaUltimaConexion",999999 AS "DiasVencimientoClave",
				    'N' AS "Conectado",'0:00:00' AS "Hora",'' AS "Terminal",'ACT' AS "IdEstado",0 AS "IntentosConexion",'0001-01-01' AS "FechaBloqueoTemporal",
				    'EMP' AS "TipoEntidad", v_IdEmpleado AS "IdEntidad",'S' AS "EsAutorizado",'N' AS "DebeCambiarClave",CURRENT_UTCTIMESTAMP AS "UltimoCambioClave",
				    '' AS "CodigoMenu",99999 AS "IdCarga",9999 AS "IdOrigen",'N','S','S','S','S'
				FROM dummy;			    
			ELSE
				--actualiza el ID de Empleado en el usuario Sistema
				
				UPDATE "BSCL.Plataforma.BD::ACC.Usuario"
				SET "IdEntidad" = v_IdEmpleado
				WHERE "CodigoUsuario" = 'SISTEMA';
				
				SELECT "IdUsuario" INTO v_IdUsuario FROM "BSCL.Plataforma.BD::ACC.Usuario" WHERE "CodigoUsuario" = 'SISTEMA';
				
			
			END IF; 

			SELECT TOP 1 "IdRol" INTO v_IdRol FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" IN ('ADMIN','FULL');
			
			--Inserta Rol usuario sistema si es que no lo tiene
			INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario", "IdUsuario", "IdRol", "EsAutorizado")
			SELECT	"BSCL.Plataforma.BD.Secuencias::RolUsuario".NEXTVAL AS "IdRolUsuario"
					,v_IdUsuario AS "IdUsuario"
					,v_IdRol AS "IdRol"
					, 'S' AS "EsAutorizado"
			FROM "BSCL.Plataforma.BD::ACC.Usuario" usu
			WHERE usu."IdUsuario" = v_IdUsuario AND NOT EXISTS(SELECT 1 FROM "BSCL.Plataforma.BD::ACC.RolUsuario" rol WHERE rol."IdUsuario" = usu."IdUsuario" AND rol."IdRol" = v_IdRol  ); 
			
		END IF;
		
END;


SET SCHEMA BSCL;
CREATE LOCAL TEMPORARY TABLE #USUARIOSBSXSN2("CODIGOUSUARIO"  NVARCHAR(50), "IDROL" NVARCHAR(10));
CREATE LOCAL TEMPORARY TABLE #USUARIOSBSXEXPLOTACION("CODIGOUSUARIO"  NVARCHAR(50), "IDROL" NVARCHAR(10));
INSERT INTO #USUARIOSBSXEXPLOTACION VALUES ('EXPLOTACION', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('ACERPA', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('CVILLABLANCA', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('CSABAG', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('CFUENTES', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('CNAVARRO', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('FPERALES', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('FGUZMAN', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('GFIGUEROA', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('ICEBALLOS', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('MMORENO', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('MCASTRO', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('LMOLINA', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO

INSERT INTO #USUARIOSBSXSN2 VALUES ('JLANDEO', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%SOPORTE%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('PMORAGA', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%SOPORTE%'));GO


INSERT INTO #USUARIOSBSXSN2 VALUES ('JGONZALEZ1', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));
INSERT INTO #USUARIOSBSXSN2 VALUES ('CSANCHEZ1', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO
INSERT INTO #USUARIOSBSXSN2 VALUES ('FGORDIAN1', (SELECT TOP 1 "IdRol" FROM "BSCL.Plataforma.BD::ACC.Rol" WHERE "Nombre" LIKE '%FULL%'));GO

--SELECT * FROM #USUARIOSBSXSN2;
--SELECT * FROM #USUARIOSBSXEXPLOTACION;
--SELECT * FROM BSCL."BSCL.SocioNegocio.BD::EMP.Empleado"

INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" (
    "IdUsuario","IdEmpresa","CodigoUsuario",
    "Clave","FechaIngreso",
    "FechaVigencia","FechaUltimaConexion","DiasVencimientoClave",
    "Conectado","Hora","Terminal","IdEstado","IntentosConexion","FechaBloqueoTemporal",
    "TipoEntidad","IdEntidad","EsAutorizado","DebeCambiarClave","UltimoCambioClave",
    "CodigoMenu","IdCarga","IdOrigen","UsuarioClientes","UsuarioNegocios","UsuarioServicios","UsuarioCorredores","EsUsuarioBolsa")
SELECT
    "BSCL"."BSCL.Plataforma.BD.Secuencias::Usuario" .NEXTVAL ,1 AS "IdEmpresa", tmp."CODIGOUSUARIO" AS "CodigoUsuario",
    '18466945D7814D8F2939B8AB90DDD6B5C6D590A03EBBBA06016DD6A55815ED80' AS "Clave",'0001-01-01' AS "FechaIngreso",
    '2999-12-31' AS "FechaVigencia",'0001-01-01' AS "FechaUltimaConexion",999999 AS "DiasVencimientoClave",
    'N' AS "Conectado",'0:00:00' AS "Hora",'' AS "Terminal",'ACT' AS "IdEstado",0 AS "IntentosConexion",'0001-01-01' AS "FechaBloqueoTemporal",
    'EMP' AS "TipoEntidad","IdEmpleado" AS "IdEntidad",'S' AS "EsAutorizado",'N' AS "DebeCambiarClave",CURRENT_UTCTIMESTAMP AS "UltimoCambioClave",
    '' AS "CodigoMenu",99999 AS "IdCarga",9999 AS "IdOrigen",'N','S','S','S','S'
FROM BSCL."BSCL.SocioNegocio.BD::EMP.Empleado", #USUARIOSBSXSN2 AS tmp
WHERE "CodigoEmpleado" = 'SISTEMA';

INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" (
    "IdUsuario","IdEmpresa","CodigoUsuario",
    "Clave","FechaIngreso",
    "FechaVigencia","FechaUltimaConexion","DiasVencimientoClave",
    "Conectado","Hora","Terminal","IdEstado","IntentosConexion","FechaBloqueoTemporal",
    "TipoEntidad","IdEntidad","EsAutorizado","DebeCambiarClave","UltimoCambioClave",
    "CodigoMenu","IdCarga","IdOrigen","UsuarioClientes","UsuarioNegocios","UsuarioServicios","UsuarioCorredores","EsUsuarioBolsa")
SELECT
    "BSCL"."BSCL.Plataforma.BD.Secuencias::Usuario" .NEXTVAL ,1 AS "IdEmpresa", tmp."CODIGOUSUARIO" AS "CodigoUsuario",
    '18466945D7814D8F2939B8AB90DDD6B5C6D590A03EBBBA06016DD6A55815ED80' AS "Clave",'0001-01-01' AS "FechaIngreso",
    '2999-12-31' AS "FechaVigencia",'0001-01-01' AS "FechaUltimaConexion",999999 AS "DiasVencimientoClave",
    'N' AS "Conectado",'0:00:00' AS "Hora",'' AS "Terminal",'ACT' AS "IdEstado",0 AS "IntentosConexion",'0001-01-01' AS "FechaBloqueoTemporal",
    'EMP' AS "TipoEntidad","IdEmpleado" AS "IdEntidad",'S' AS "EsAutorizado",'N' AS "DebeCambiarClave",CURRENT_UTCTIMESTAMP AS "UltimoCambioClave",
    '' AS "CodigoMenu",99999 AS "IdCarga",9999 AS "IdOrigen",'N','S','S','S','S'
FROM BSCL."BSCL.SocioNegocio.BD::EMP.Empleado", #USUARIOSBSXEXPLOTACION AS tmp
WHERE "CodigoEmpleado" = 'SIST';GO

CALL "BSCL.Plataforma.BD.Servicios::ACC.CrearRol"(
p_IdEmpresa					=> 1,
p_IdUsuario					=> 0,
p_IdModulo					=> 'X',
p_Nombre					=> 'SN2',
p_IdEmpresaRol				=> 1, 	
p_IdEstado					=>  'ACT',
p_out_new_rol_id 			=> ?,
p_out_AdvertenciasContador 	=> ?,
p_out_AdvertenciasDetalle 	=> ?,	
p_out_ErroresContador 		=> ?,
p_out_ErroresDetalle 		=> ?);

CALL "BSCL.Plataforma.BD.Servicios::ACC.CrearRol"(
p_IdEmpresa					=> 1,
p_IdUsuario					=> 0,
p_IdModulo					=> 'X',
p_Nombre					=> 'EXPLOTACION',
p_IdEmpresaRol				=> 1, 	
p_IdEstado					=>  'ACT',
p_out_new_rol_id 			=> ?,
p_out_AdvertenciasContador 	=> ?,
p_out_AdvertenciasDetalle 	=> ?,	
p_out_ErroresContador 		=> ?,
p_out_ErroresDetalle 		=> ?);

CALL "BSCL.Plataforma.BD.Servicios::ACC.CrearPerfil"(
p_IdEmpresa					=> 1,
p_IdUsuario					=> 0,
p_IdModulo					=> 'X',
p_Nombre					=> 'EXPLOTACION',
p_IdEstado					=>  'ACT',
p_out_new_perfil_id 			=> ?,
p_out_AdvertenciasContador 	=> ?,
p_out_AdvertenciasDetalle 	=> ?,	
p_out_ErroresContador 		=> ?,
p_out_ErroresDetalle 		=> ?);

CALL "BSCL.Plataforma.BD.Servicios::ACC.CrearPerfil"(
p_IdEmpresa					=> 1,
p_IdUsuario					=> 0,
p_IdModulo					=> 'X',
p_Nombre					=> 'SN2',
p_IdEstado					=>  'ACT',
p_out_new_perfil_id 			=> ?,
p_out_AdvertenciasContador 	=> ?,
p_out_AdvertenciasDetalle 	=> ?,	
p_out_ErroresContador 		=> ?,
p_out_ErroresDetalle 		=> ?);


INSERT INTO BSCL."BSCL.Plataforma.BD::ACC.RolPerfil" 
("IdRolPerfil", "IdRol", "IdPerfil", "IdEstado") 
SELECT
"BSCL.Plataforma.BD.Secuencias::RolPerfil".NEXTVAL as "IdRolPerfil",
(select "IdRol" FROM BSCL."BSCL.Plataforma.BD::ACC.Rol" where "Nombre" = 'SOPORTE' ) as "IdRol",
(select "IdPerfil" FROM BSCL."BSCL.Plataforma.BD::ACC.Perfil" where "Nombre" = 'SOPORTE' ) as "IdPerfil",'ACT' as "IdEstado"
FROM DUMMY;

INSERT INTO BSCL."BSCL.Plataforma.BD::ACC.RolPerfil" 
("IdRolPerfil", "IdRol", "IdPerfil", "IdEstado") 
SELECT
"BSCL.Plataforma.BD.Secuencias::RolPerfil".NEXTVAL as "IdRolPerfil",
(select "IdRol" FROM BSCL."BSCL.Plataforma.BD::ACC.Rol" where "Nombre" = 'EXPLOTACION' ) as "IdRol",
(select "IdPerfil" FROM BSCL."BSCL.Plataforma.BD::ACC.Perfil" where "Nombre" = 'EXPLOTACION' ) as "IdPerfil",'ACT' as "IdEstado"
FROM DUMMY;

INSERT INTO BSCL."BSCL.Plataforma.BD::ACC.PerfilAcceso" ("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado", "FechaAsignacion", 
"AsignoUsuario") 
SELECT "BSCL.Plataforma.BD.Secuencias::PerfilAcceso".NEXTVAL,1,
(select "IdPerfil" FROM BSCL."BSCL.Plataforma.BD::ACC.Perfil" where "Nombre" = 'EXPLOTACION' ),
"IdAcceso",'ACT',CURRENT_TIMESTAMP,0
FROM BSCL."BSCL.Plataforma.BD::ACC.Acceso" 
WHERE "NombreAcceso" like '%auta%'


INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario", "IdUsuario", "IdRol", "EsAutorizado")
SELECT	"BSCL.Plataforma.BD.Secuencias::RolUsuario".NEXTVAL AS "IdRolUsuario"
		,"IdUsuario"
		,(select "IdRol" FROM BSCL."BSCL.Plataforma.BD::ACC.Rol" where "Nombre" = 'EXPLOTACION') AS "IdRol"
		, 'S' AS "EsAutorizado"
FROM "BSCL.Plataforma.BD::ACC.Usuario" usu
WHERE "CodigoUsuario" = 'EXPLOTACION';

INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario", "IdUsuario", "IdRol", "EsAutorizado")
SELECT	"BSCL.Plataforma.BD.Secuencias::RolUsuario".NEXTVAL AS "IdRolUsuario"
		,"IdUsuario"
		,(select "IdRol" FROM BSCL."BSCL.Plataforma.BD::ACC.Rol" where "Nombre" = 'SOPORTE') AS "IdRol"
		, 'S' AS "EsAutorizado"
FROM "BSCL.Plataforma.BD::ACC.Usuario" usu
WHERE "CodigoUsuario" IN (select CODIGOUSUARIO from #USUARIOSBSXSN2);


INSERT INTO BSCL."BSCL.Plataforma.BD::ACC.PerfilAcceso" ("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado", "FechaAsignacion", 
"AsignoUsuario") 
SELECT "BSCL.Plataforma.BD.Secuencias::PerfilAcceso".NEXTVAL,1,
(select "IdPerfil" FROM BSCL."BSCL.Plataforma.BD::ACC.Perfil" where "Nombre" = 'SN2' ),
"IdAcceso",'ACT',CURRENT_TIMESTAMP,0
FROM BSCL."BSCL.Plataforma.BD::ACC.Acceso";

DROP TABLE #USUARIOSBSXSN2;
DROP TABLE #USUARIOSBSXEXPLOTACION;
commit;
