-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET schema BSCL;
--
--
-- LIBERACION SEMANA: 12-02-2016
--


---------------------------
-- Inscripción de servicios para eliminar Cuentas Contables
---------------------------

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(163,'BSCL.Tesoreria.BD.Servicios::EliminarCuentaBancaria',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(161,'BSCL.Tesoreria.BD.Servicios::CrearChequera',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(166,'BSCL.Tesoreria.BD.Servicios::ModificarChequera',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(177,'BSCL.Tesoreria.BD.Servicios::BloquearDesbloquearChequera',0,'P','');

-----------------------------------
-- Nuevo Tipo de Registro para LOG de edición de Cuentas Bancarias 
-----------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro","Descripcion")
VALUES('CUENTABANCARIA','CUENTABANCARIA');


-----------------------------------
-- Anadir campos a entidad Banco
-----------------------------------

ALTER TABLE "BSCL"."BSCL.Plataforma.BD::BAN.Banco" ADD ("RutBanco"     NVARCHAR(25)  not null default '');
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::BAN.Banco" ADD ("CodigoSwift"  NVARCHAR(40)  not null default '');
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::BAN.Banco" ADD ("CodigoAba"    NVARCHAR(40)  not null default '');
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::BAN.Banco" ADD ("CodigoIban"   NVARCHAR(40)  not null default '');


UPDATE "BSCL"."BSCL.Plataforma.BD::BAN.Banco"
SET 	"RutBanco"    = '',
		"CodigoSwift" = '0',
		"CodigoAba"   = '0',
		"CodigoIban"  = '0';
		
		
----------------------------------------------------------------------------------------------------
--------------------------------------MODIFICACION USUARIO------------------------------------------
----------------------------------------------------------------------------------------------------
set schema "BSCL";
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(159,'BSCL.Plataforma.BD.Servicios::ACC.ActualizarUsuario',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(160,'BSCL.Plataforma.BD.Servicios::ACC.DesbloqueoTemporalUsuario',0,'P','');

UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Modulo" SET "Codigo" = 'PLA', "Nombre" = 'Plataforma'
WHERE "IdModulo" = 'PLA';

UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"
SET "IdEstado" = 'INA', "Descripcion" = 'Inactivo'
WHERE "IdEstadoPla" = 9;

UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"
SET "IdEstado" = 'INA', "Descripcion" = 'Inactivo'
WHERE "IdEstadoPla" = 7;

--Actualizar fechas de vigencia de los usuarios al maximo del widget datepicker.
UPDATE "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" SET "FechaVigencia" = '2099-12-30';
UPDATE "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" SET "FechaBloqueoTemporal" = '0001-01-01 00:00:00 000';  
----------------------------------------------------------------------------------------------------
----------------------------------FIN MODIFICACION USUARIO------------------------------------------
----------------------------------------------------------------------------------------------------

-----------------------------------CREACION MENU DINAMICO-----------------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'UsuarioAdministrador', 'Usuario con todos los accesos por default', 'MASTER');
---------------------------------------------------------------------------------------------------

set schema "BSCL";
delete from "BSCL.Plataforma.BD::ACC.Acceso";
ALTER TABLE "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ADD ("IdElementoHtml"   NVARCHAR(200)  unique not null);


--ROOTS
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(1,'MERCADO', 'MENU1', 0, 1, 0, 'MODULO_MERCADO', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(2,'SOCIO DE NEGOCIO', 'MENU1', 0, 2, 0, 'MODULO_SOCIO_NEGOCIO', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(3,'OPERACIONES EN RUEDA', 'MENU1', 0, 3, 0, 'MODULO_ORDEN', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(4,'CUENTA INVERSION', 'MENU1', 0, 4, 0, 'MODULO_CUENTA_INVERSION', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(5,'CUSTODIA', 'MENU1', 0, 5, 0, 'MODULO_CUSTODIA', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(6,'FACTURACION', 'MENU1', 0, 6, 0, 'MODULO_FACTURACION', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(7,'TESORERIA', 'MENU1', 0, 7, 0, 'MODULO_TESORERIA', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(8,'CONTABILIDAD', 'MENU1', 0, 8, 0, 'MODULO_CONTABILIDAD', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(9,'RENTA FIJA E IIF', 'MENU1', 0, 9, 0, 'MODULO_MESARF', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(10,'SPOT', 'MENU1', 0, 10, 0, 'MODULO_MESASPOT', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(11,'FORWARD', 'MENU1', 0, 11, 0, 'MODULO_MESAFORWARD', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(12,'NORMATIVO', 'MENU1', 0, 12, 0, 'MODULO_NORMATIVO', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(13,'PLATAFORMA', 'MENU1', 0, 13, 0, 'MODULO_PLATAFORMA', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(14,'DESARROLLO', 'MENU1', 0, 14, 0, 'MODULO_DESARROLLO', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(15, 'Homologacion', 'MENU1', 0, 15, 0, 'MODULO_HOMOLOGACION', 'S');

----------------------------ACCESOS_USUARIOS----------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(16,'Usuarios', 'MENU2', 13, 1, 0, 'ConsultaUsuarios', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(17,'Pantalla Crear Usuario', 'BTN', 16, 1 ,0 ,'ConsultaUsuarios_crearUsuario', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(18,'Pantalla Modificar Usuario', 'BTN', 16, 2 ,0 , 'ConsultaUsuarios_modificarUsuario', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(19,'Pantalla Administrar Roles', 'BTN', 16, 3, 0, 'ConsultaUsuarios_administrarRoles', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(20,'Pantalla Administrar Perfiles', 'BTN', 16, 4 ,0 , 'ConsultaUsuarios_administrarPeriles', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso","Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(21,'Pantalla Modicar Roles Usuario', 'BTN', 16, 5 ,0 , 'ConsultaUsuarios_modificarRolesUsuario', 'S');

--Accesos pantalla creacion usuario
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(22,'Crear Usuario', 'BTN',17, 1, 112, 'formCrearUsuario_crearUsuario', 'S');
--Pantalla Modificacion Usuario
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(23,'Modificar Usuario', 'BTN', 18, 1, 159, 'formModificarUsuario_modificarUsuario', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(24,'Desbloqueo Temporal Usuario', 'BTN', 18, 2, 160, 'formModificarUsuario_desbloqueoTemporalUsuario', 'S');
--Pantalla Modificacion roles usuario
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(25,'Asignar Rol Usuario', 'BTN', 21, 1, 120, 'ModificarRolesUsuario_asignarRolUsuario', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(26,'Eliminar Rol Usuario', 'BTN', 21, 2, 121, 'ModificarRolesUsuario_quitarRolUsuario', 'S');
--Pantalla administracion perfil
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(27,'Pantalla Crear Perfil', 'BTN', 20, 1, 0, 'ConsultarPerfiles_crearPerfil', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(28,'Pantalla Modificar Perfil', 'BTN', 20, 2, 0, 'ConsultarPerfiles_modificarPerfil', 'S');
--Pantalla creacion perfil
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(29,'Crear Perfil', 'BTN', 27, 1, 138, 'CrearPerfil_crePerfil', 'S');
--Pantalla modificacion perfil
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(30,'Sin autorizacion', 'BTN', 28, 1, 148, 'ModificarPerfil_sinAutorizacion', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(31,'Autorizacion Total', 'BTN', 28, 2, 148, 'ModificarPerfil_autorizacionTotal', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(32,'Actualizar Usuario', 'BTN', 28, 3, 148, 'ModificarPerfil_actualizarPerfil', 'S');
--Pantalla administracion rol
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(33,'Pantalla Crear Rol', 'BTN', 19, 1, 0, 'ConsultaRoles_crearRol', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(34,'Pantalla Modificar Rol', 'BTN', 19, 2, 0, 'ConsultaRoles_modificarRol', 'S');
--Pantalla creacion rol
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(35,'Crear Rol', 'BTN', 33, 1, 126, 'CrearRol_crearRol', 'S');
--Pantalla modificacion rol
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(36,'Actualizar Rol', 'BTN', 34, 1, 132, 'ModificarRoles_actualizarRol', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(37,'Asignar Rol', 'BTN', 34, 2, 128, 'ModificarRoles_asignarRol', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(38,'Quitar Rol', 'BTN', 34, 3, 129, 'ModificarRoles_quitarRol', 'S');


----------------------------ACCESOS_SERVICIOS_SISTEMA--------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100001,'Servicio Tiempo', 'SERV', 0, 0, 14, 'SERVICIO_TIEMPO', 'N');

----------------------------FIN_ACCESOS_USUARIOS----------------------------

----------------------------PERFIL_ACCESO--------------------------------
set schema "BSCL";
delete from "BSCL.Plataforma.BD::ACC.PerfilAcceso";
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (1,1,1,13,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (2,1,1,14,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (3,1,1,15,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (4,1,1,16,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (5,1,1,17,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (6,1,1,18,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (7,1,1,19,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (8,1,1,20,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (9,1,1,21,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (10,1,1,22,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (11,1,1,23,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (12,1,1,24,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (13,1,1,25,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (14,1,1,26,'ACT');

-----------------------------------FIN CREACION MENU DINAMICO-----------------------------------------
---------------------------------------------------------------------------------------------------

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(174,'BSCL.Orden.BD.Servicios::ProgramarSimulacionAsignacion',0,'P','');


--####################################################
--			JG.AGREGA VALORES COBRO
--####################################################

	-- INRF
	INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
	VALUES ('COVA_INRF_A','COVA_INRF_A','INRF','COVA',0.82,2000,'CLP','2015-11-05','ACT',1);
	INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
	VALUES ('COVA_INRF_B','COVA_INRF_B','INRF','COVA',0.41,0,'CLP','2015-11-05','ACT',1);
	INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
	VALUES ('DEFI_INRF_A','DEFI_INRF_A','INRF','DEFI',3000,0,'CLP','2015-11-05','ACT',1);
	INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
	VALUES ('GFOD_INRF_A','GFOD_INRF_A','INRF','GFOD',5700,0,'CLP','2015-11-05','ACT',1);
	INSERT INTO "BSCL.Plataforma.BD::COM.ValorCobro" ("IdValorCobro","DescripcionValorCobro","IdTipoProducto","IdCobro","ValorCobro","ValorTopeMinimoOrden","IdMoneda","FechaCreacion","IdEstado","IdEmpresa")
	VALUES ('IVA_INRF_A','IVA_INRF_A','INRF','IVA',19,0,'CLP','2015-11-05','ACT',1);

-------------------------------------------------------
--			FIN AGREGA VALORES COBRO
-------------------------------------------------------


--####################################################
--			JG.OPERADORES
--####################################################

	-- CREACION DE TABLAS/SECUENCIAS
	drop table "BSCL.Plataforma.BD::TRA.ConfiguracionOperador"				cascade;
	drop table "BSCL.Plataforma.BD::TRA.HomologacionOperador"				cascade;
	
	create column table "BSCL.Plataforma.BD::TRA.ConfiguracionOperador" ( 
		"CodigoOperador"			NVARCHAR(10)		not null,
		"CodigoCorredora"			NVARCHAR(10)		not null default '',
		"UtilizaPortafolio"			NVARCHAR(1)			not null default 'N',
		--"IdSocioNegocio"			BIGINT				not null default 0,
		"IdPortafolio"				BIGINT				not null default 0,
		"ReservaTransaccion"		NVARCHAR(1)			not null default 'N',
		"AsignMRV"					NVARCHAR(1)			not null default 'N',
		"MvtoFactMRV"				NVARCHAR(1)			not null default 'N',
		"AsignMMILA"				NVARCHAR(1)			not null default 'N',
		"MvtoFactMMILA"				NVARCHAR(1)			not null default 'N',
		"AsignMDEUDA"				NVARCHAR(1)			not null default 'N',
		"MvtoFactMDEUDA"			NVARCHAR(1)			not null default 'N',
		"AsignMDERIV"				NVARCHAR(1)			not null default 'N',
		"MvtoFactMDERIV"			NVARCHAR(1)			not null default 'N',
		"AsignMOTC"					NVARCHAR(1)			not null default 'N',
		"MvtoFactMOTC"				NVARCHAR(1)			not null default 'N',
		"IdEstado"					NVARCHAR(3)    		not null default 'ACT',
		"FechaCreacion"				DATE				not null default '0001-01-01',
		"FechaModificacion"			DATE				not null default '0001-01-01',
		"IdUsuarioCrea"				BIGINT				not null default 0, 
		"IdUsuarioModifica"			BIGINT				not null default 0,
		"IdEmpresa"					BIGINT				not null default 0,
		primary key ("CodigoOperador")
	);
	
	create column table "BSCL.Plataforma.BD::TRA.HomologacionOperador" ( 
		"IdHomologacionOperador"	BIGINT				not null,
		"CodigoOperador"			NVARCHAR(10)		not null default '',
		"IdBolsa"					NVARCHAR(10)		not null default '',
		"CodigoHomologacion"		NVARCHAR(10)		not null default '',
		"IdEstado"					NVARCHAR(3)    		not null default 'ACT',
		"FechaCreacion"				DATE				not null default '0001-01-01',
		"FechaModificacion"			DATE				not null default '0001-01-01',
		"IdUsuarioCrea"				BIGINT				not null default 0, 
		"IdUsuarioModifica"			BIGINT				not null default 0,
		"IdEmpresa"					BIGINT				not null default 0,
		primary key ("IdHomologacionOperador")
	);
	drop sequence "BSCL.Plataforma.BD.Secuencias::HomologacionOperador";
	create sequence "BSCL.Plataforma.BD.Secuencias::HomologacionOperador" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

	-- INSCRIPCION DE SERVICIOS
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
	VALUES(167,'BSCL.Plataforma.BD.Servicios::TRA.CrearHomologacionOperador',0,'P','');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
	VALUES(168,'BSCL.Plataforma.BD.Servicios::TRA.CrearConfiguracionOperador',0,'P','');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
	VALUES(171,'BSCL.Plataforma.BD.Servicios::TRA.ModificarHomologacionOperador',0,'P','');
	INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
	VALUES(172,'BSCL.Plataforma.BD.Servicios::TRA.ModificarConfiguracionOperador',0,'P','');

-------------------------------------------------------
--			FIN OPERADORES
-------------------------------------------------------

-------------------------------------------------------
--			NOMBRE FANTASIA
-------------------------------------------------------

  create column table "BSCL.SocioNegocio.BD::PER.NombreFantasia" (
   "IdNombreFantasia"  			BIGINT     			not null,
   "DescripciopnNombreFantasia" NVARCHAR(200)  		not null,	
   "IdPersona"			  		BIGINT     			not null,	
   "FechaCreacion"     			SECONDDATE          default '0001-01-01 00:00:00' not null,
   "FechaModificacion"  		SECONDDATE          default '0001-01-01 00:00:00' not null,
   "FechaIngreso"       		SECONDDATE          default '0001-01-01 00:00:00' not null,
   "IdEstado" 				 	NVARCHAR(3) 		DEFAULT 'ACT' NOT NULL ,
   "IdEstadoAutorizacion" 		NVARCHAR(3)         default 'PEN' not null,
   "Borrado" 					NVARCHAR(1) 		DEFAULT 'N',
   "IdUsuarioCreador"   		BIGINT  			DEFAULT 0,
   primary key ("IdNombreFantasia")
);


create sequence "BSCL.SocioNegocio.BD.Secuencias::NombreFantasia" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle ;

alter table "BSCL.SocioNegocio.BD::PER.NombreFantasia"
   add constraint "FK_Persona_NombreFantasia" foreign key ("IdPersona")
	 references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona");
	 SET SCHEMA "BSCL";
	 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('NUENFA','','SOC','NUEVO NOMBRE FANTASIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CAENFA','','SOC','CAMBIO ESTADO NOMBRE FANTASIA','VIG');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(165,'BSCL.SocioNegocio.BD.Servicios::PER.ModificarEstadoPersonaNombreFantasia',0,'P','');

-------------------------------------------------------
--			FIN NOMBRE FANTASIA
-------------------------------------------------------

-------------------------------------------------------
--			AGREGA ESTADO TIPO ATRIBUTO
-------------------------------------------------------

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.EstadoTipoAtributo" (
 "IdEstadoTipoAtributo" BIGINT CS_FIXED NOT NULL ,
 "IdEstado" NVARCHAR(3) DEFAULT '' NOT NULL ,
 "NombreEntidad" NVARCHAR(100) DEFAULT '' NOT NULL , 
 PRIMARY KEY ("IdEstadoTipoAtributo"))  ;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoTipoAtributo" values(1,'ACT','ATR.TipoAtributo');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoTipoAtributo" values(2,'INA','ATR.TipoAtributo');

-------------------------------------------------------
--			FIN ESTADO TIPO ATRIBUTO
-------------------------------------------------------


-------------------------------------------------------
--	JG.MODIFICACION SP CREACION PLANTILLA EMPLEADO
-------------------------------------------------------

UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" 
SET "RutaServicio" = 'BSCL.Plataforma.BD.Servicios::COM.CrearPlantillaEmpleado'
WHERE "IdServicio" = 143;

-------------------------------------------------------
--	FIN MODIFICACION SP CREACION PLANTILLA EMPLEADO
-------------------------------------------------------
