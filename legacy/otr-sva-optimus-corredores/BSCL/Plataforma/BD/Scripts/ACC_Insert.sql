SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM  "BSCL.Plataforma.BD::ACC.Acceso";
DELETE FROM  "BSCL.Plataforma.BD::ACC.PerfilAcceso";
DELETE FROM  "BSCL.Plataforma.BD::ACC.RolPerfil"; 
DELETE FROM  "BSCL.Plataforma.BD::ACC.Perfil";
DELETE FROM  "BSCL.Plataforma.BD::ACC.RolUsuario";
DELETE FROM  "BSCL.Plataforma.BD::ACC.Rol";
DELETE FROM  "BSCL.Plataforma.BD::ACC.Sesion";
DELETE FROM  "BSCL.Plataforma.BD::ACC.Usuario"; 

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::ACC.Perfil"("IdPerfil","IdEmpresa","Nombre") VALUES (1,1,'PERFIL 1');

INSERT INTO "BSCL.Plataforma.BD::ACC.Rol"("IdRol","IdEmpresa","Nombre","IdEstado") VALUES (1,1,'ROL 1','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Rol"("IdRol","IdEmpresa","Nombre","IdEstado") VALUES(2, 1, 'SEGURIDAD', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" ( "IdUsuario","IdEmpresa","CodigoUsuario","Clave","FechaIngreso","FechaVigencia","FechaUltimaConexion","DiasVencimientoClave","Conectado","Hora","Terminal","IdEstado","IntentosConexion","FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado" )  
VALUES(2,1,'CACO','F2FFA29368E87A301E177B80DB40B5497249C1D75CCF887DEB49CBCB81AF59F8', '2015-07-28','2099-12-30','2015-07-28',100.00000000,'S','17:15:12','Terminal Nro','VIG',0,'0001-01-01 00:00:00.000000000', 'CLI', 10000, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" ( "IdUsuario","IdEmpresa","CodigoUsuario","Clave","FechaIngreso","FechaVigencia","FechaUltimaConexion","DiasVencimientoClave","Conectado","Hora","Terminal","IdEstado","IntentosConexion","FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado")
VALUES(1,1,'USUARIO1','298E25A9F5D712F6C530628EE9F0203A6FB1ED139884AA17A29B526F8DB52119','2015-07-28','2099-12-30','2015-10-08',100.00000000,'S','18:27:10','Terminal Nro','VIG',0,'0001-01-01 00:00:00.000000000', 'CLI', 10001, 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" ( "IdUsuario","IdEmpresa","CodigoUsuario","Clave","FechaIngreso","FechaVigencia","FechaUltimaConexion","DiasVencimientoClave","Conectado","Hora","Terminal","IdEstado","IntentosConexion","FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado")  
VALUES(3,1,'MASTER','4F04F35BEE3556C6E80572D9174BEA77ADB0FE153D2F018AD6AECD21D4BF7633','2015-07-28','2099-12-30','2015-10-13',100.00000000,'S','12:15:42','Terminal Nro','VIG',0,'2015-10-02 17:23:20.252000000', 'CLI', 10002, 'S');

INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario","IdUsuario","IdRol") VALUES (1,1,1,'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario","IdUsuario","IdRol") VALUES (2,2,1,'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario" ("IdRolUsuario","IdUsuario","IdRol") VALUES (3,3,1,'S');


INSERT INTO "BSCL.Plataforma.BD::ACC.RolPerfil" ("IdRolPerfil","IdRol","IdPerfil","IdEstado") VALUES (1,1,1,'ACT');


INSERT INTO "BSCL.Plataforma.BD::ACC.Sesion" ( "IdTokenSesion","IdUsuario","TokenValido","FechaUltimoUso","UsoToken","FechaIngreso","FechaSalida" )  
VALUES('MTp1c3VhcmlvMTo1NURFOERCNTQ4N0QwREVCRTIwMEQ3NkE3OEQ4MTRGRg==',1,'S','2015-08-26 23:35:14.596000000',2,'2015-08-26 23:35:04.698000000','0001-01-01 00:00:00.000000000');


/*==============================================================*/
/* Insert Table ACCESOS                                         */
/*==============================================================*/
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


------------------------------------------------------------------------------------
----------------------------ACCESOS_SOCIO_DE_NEGOCIO--------------------------------
------------------------------------------------------------------------------------

--Submenú Crear Cliente
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(39,'Pantalla crear cliente', 'MENU2', 2, 2, 0, 'CrearCliente', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(40,'Botón Crear Cliente', 'BTN', 39, 1, 54, 'formCrearCrearCliente_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(188,'Botón Guardar Borrador Cliente', 'BTN', 39, 2, 52, 'formCrearCrearCliente_guardar_borrador', 'S');
--FIX Ingresar cliente
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(274,'Buscar Documento Identidad', 'BTN', 39, 3, 53, 'formCrearCrearCliente_documento_identidad', 'S');

--Submenú Socios de Negocio
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(41,'Socios de Negocio', 'MENU2', 2, 1, 0, 'ConsultaSocioNegocio', 'S');
--Botón a Pantalla Crear Cliente en la pantalla de Socios de Negocio
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(42,'Botón a Pantalla Crear Cliente', 'BTN', 41, 1, 0, 'ConsultaSocioNegocio_crearCliente', 'S');

---------------------------------------------------------------------------------------------
--------------------------------Pestaña PERSONA----------------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(43,'Pestaña Persona', 'BTN', 41, 2, 0, 'TabPersona', 'S');


--Pestaña Datos Generales de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(44,'Pestaña Datos Generales', 'BTN', 43, 1, 0, 'TabPersonaTabDatosGenerales', 'S');
--Botones en pestaña Datos Generales de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(45,'Editar Datos Generales', 'BTN', 44, 1, 0, 'PanelPersonaDatosGenerales_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(46,'Guardar Datos Generales', 'BTN', 45, 1, 68, 'PanelPersonaDatosGenerales_guardar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(47,'Bloquear Datos Generales', 'BTN', 44, 2, 108, 'PanelPersonaDatosGenerales_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(48,'Desbloquear Datos Generales', 'BTN', 44, 3, 108, 'PanelPersonaDatosGenerales_desbloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(49,'Solicitar Autorización Enrolamiento', 'BTN', 44, 4, 108, 'PanelPersonaDatosGenerales_solAutEnrol', 'S');


--Pestaña Documentos Identidad de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(50,'Pestaña Documentos Identidad', 'BTN', 43, 2, 0, 'TabPersonaTabDocumentosIdentidad', 'S');
--Botones en pestaña Documentos de Identidad de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(51,'Agregar Documento Identidad', 'BTN', 50, 1, 0, 'PanelPersonaDocumentosIdentidad_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(52,'Modificar Documento Identidad', 'BTN', 50, 2, 0, 'PanelPersonaDocumentosIdentidad_modificar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(53,'Eliminar Documento Identidad', 'BTN', 50, 3, 0, 'PanelPersonaDocumentosIdentidad_eliminar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(54,'Bloquear Documento Identidad', 'BTN', 50, 4, 100, 'PanelPersonaDocumentosIdentidad_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(55,'Desbloquear Documento Identidad', 'BTN', 50, 5, 100, 'PanelPersonaDocumentosIdentidad_desbloquear', 'S');
--Botones en pantalla Modal Documento de Identidad
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(56,'Aceptar Agregar Documento Identidad', 'BTN', 51, 1, 72, 'ModalDocumentoIdentidad_aceptar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(57,'Aceptar Modificar Documento Identidad', 'BTN', 52, 1, 74, 'ModalDocumentoIdentidad_aceptarMod', 'S');


--Pestaña Cuentas Bancarias de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(58,'Pestaña Cuentas Bancarias', 'BTN', 43, 3, 0, 'TabPersonaTabCuentasBancarias', 'S');
--Botones en pestaña Cuentas Bancarias de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(59,'Agregar Cuenta Bancaria', 'BTN', 58, 1, 0, 'PanelPersonaCuentasBancarias_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(60,'Modificar Cuenta Bancaria', 'BTN', 58, 2, 0, 'PanelPersonaCuentasBancarias_modificar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(61,'Eliminar Cuenta Bancaria', 'BTN', 58, 3, 0, 'PanelPersonaCuentasBancarias_eliminar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(62,'Bloquear Cuenta Bancaria', 'BTN', 58, 4, 116, 'PanelPersonaCuentasBancarias_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(63,'Desbloquear Cuenta Bancaria', 'BTN', 58, 5, 116, 'PanelPersonaCuentasBancarias_desbloquear', 'S');
--Botones en pantalla Modal Cuenta Bancaria
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(64,'Aceptar Agregar Cuenta Bancaria', 'BTN', 59, 1, 75, 'ModalCuentaBancaria_aceptar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(65,'Aceptar Modificar Cuenta Bancaria', 'BTN', 60, 1, 76, 'ModalCuentaBancaria_aceptarMod', 'S');


--Pestaña Tarjetas Bancarias de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(66,'Pestaña Tarjetas Bancarias', 'BTN', 43, 4, 0, 'TabPersonaTabTarjetasBancarias', 'S');
--Botones en pestaña Tarjetas Bancarias de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(67,'Agregar Tarjeta Bancaria', 'BTN', 66, 1, 0, 'PanelPersonaTarjetasBancarias_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(68,'Modificar Tarjeta Bancaria', 'BTN', 66, 2, 0, 'PanelPersonaTarjetasBancarias_modificar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(69,'Eliminar Tarjeta Bancaria', 'BTN', 66, 3, 0, 'PanelPersonaTarjetasBancarias_eliminar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(70,'Bloquear Tarjeta Bancaria', 'BTN', 66, 4, 115, 'PanelPersonaTarjetasBancarias_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(71,'Desbloquear Tarjeta Bancaria', 'BTN', 66, 5, 115, 'PanelPersonaTarjetasBancarias_desbloquear', 'S');
--Botones en pantalla Modal Tarjeta Bancaria
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(72,'Aceptar Agregar Tarjeta Bancaria', 'BTN', 67, 1, 92, 'ModalTarjetaBancaria_aceptar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(73,'Aceptar Modificar Tarjeta Bancaria', 'BTN', 68, 1, 93, 'ModalTarjetaBancaria_aceptarMod', 'S');


--Pestaña Telefonos de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(74,'Pestaña Telefonos', 'BTN', 43, 5, 0, 'TabPersonaTabTelefonos', 'S');
--Botones en pestaña Telefonos de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(75,'Agregar Telefono', 'BTN', 74, 1, 0, 'PanelPersonaTelefonos_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(76,'Modificar Telefono', 'BTN', 74, 2, 0, 'PanelPersonaTelefonos_modificar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(77,'Eliminar Telefono', 'BTN', 74, 3, 0, 'PanelPersonaTelefonos_eliminar', 'S');
--Botones en pantalla Modal Telefono
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(78,'Aceptar Agregar Telefono', 'BTN', 75, 1, 79, 'ModalTelefono_aceptar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(79,'Aceptar Modificar Telefono', 'BTN', 76, 1, 80, 'ModalTelefono_aceptarMod', 'S');


--Pestaña Direcciones Fisicas de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(80,'Pestaña Direcciones Físicas', 'BTN', 43, 6, 0, 'TabPersonaTabDireccionesFisicas', 'S');
--Botones en pestaña Direcciones Fisicas de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(81,'Agregar Dirección Fisica', 'BTN', 80, 1, 0, 'PanelPersonaDireccionesFisicas_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(82,'Modificar Dirección Fisica', 'BTN', 80, 2, 0, 'PanelPersonaDireccionesFisicas_modificar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(83,'Eliminar Dirección Fisica', 'BTN', 80, 3, 0, 'PanelPersonaDireccionesFisicas_eliminar', 'S');
--Botones en pantalla Modal Dirección Fisica
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(84,'Aceptar Agregar Dirección Fisica', 'BTN', 81, 1, 81, 'ModalDireccionFisica_aceptar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(85,'Aceptar Modificar Dirección Fisica', 'BTN', 82, 1, 82, 'ModalDireccionFisica_aceptarMod', 'S');


--Pestaña Direcciones Electronicas de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(86,'Pestaña Direcciones Electrónicas', 'BTN', 43, 7, 0, 'TabPersonaTabDireccionesElectronicas', 'S');
--Botones en pestaña Direcciones Electrónicas de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(87,'Agregar Dirección Electrónica', 'BTN', 86, 1, 0, 'PanelPersonaDireccionesElectronicas_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(88,'Modificar Dirección Electrónica', 'BTN', 86, 2, 0, 'PanelPersonaDireccionesElectronicas_modificar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(89,'Eliminar Dirección Electrónica', 'BTN', 86, 3, 0, 'PanelPersonaDireccionesElectronicas_eliminar', 'S');
--Botones en pantalla Modal Dirección Electrónica
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(90,'Aceptar Agregar Dirección Electrónica', 'BTN', 87, 1, 83, 'ModalDireccionElectronica_aceptar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(91,'Aceptar Modificar Dirección Electrónica', 'BTN', 88, 1, 84, 'ModalDireccionElectronica_aceptarMod', 'S');










--Pestaña Grupo Direcciones Electronicas de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(279,'Pestaña Grupo Direcciones Electrónicas', 'BTN', 43, 7, 0, 'TabPersonaTabGrupoDireccionesElectronicas', 'S');
--Botones en pestaña Grupo Direcciones Electrónicas de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(280,'Agregar Grupo Dirección Electrónica', 'BTN', 279, 1, 0, 'PanelPersonaGrupoDireccionesElectronicas_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(281,'Modificar Grupo Dirección Electrónica', 'BTN', 279, 2, 0, 'PanelPersonaGrupoDireccionesElectronicas_modificar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(282,'Eliminar Grupo Dirección Electrónica', 'BTN', 279, 3, 0, 'PanelPersonaGrupoDireccionesElectronicas_eliminar', 'S');
--Botones en pantalla Modal Dirección Electrónica
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(283,'Aceptar Agregar Grupo Dirección Electrónica', 'BTN', 280, 1, 239, 'ModalGrupoDireccionElectronica_aceptar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(284,'Aceptar Modificar Grupo Dirección Electrónica', 'BTN', 281, 1, 242, 'ModalGrupoDireccionElectronica_cancelar', 'S');












--Pestaña Atributos de la pestaña PERSONA, Portafolio CLIENTE, CLIENTE,
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(92,'Pestaña Atributos', 'BTN', 43, 8, 63, 'TabPersonaTabAtributos', 'S');
--Botones en pestaña Atributos de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(93,'Editar Atributos', 'BTN', 92, 1, 0, 'BcsGridAtributos_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(94,'Modificar Atributos', 'BTN', 92, 2, 65, 'BcsGridAtributos_modificar', 'S');


--Pestaña Relaciones de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(95,'Pestaña Relaciones', 'BTN', 43, 9, 0, 'TabPersonaTabRelaciones', 'S');
--Botones en pestaña Relaciones de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(96,'Agregar Relación', 'BTN', 95, 1, 0, 'PanelPersonaRelaciones_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(97,'Modificar Relación', 'BTN', 95, 2, 0, 'PanelPersonaRelaciones_modificar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(98,'Bloquear Relación', 'BTN', 95, 3, 153, 'PanelPersonaRelaciones_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(99,'Desbloquear Relación', 'BTN', 95, 4, 153, 'PanelPersonaRelaciones_desbloquear', 'S');
--Botones en pantalla Modal Relación
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100,'Pantalla Crear Persona', 'BTN', 96, 1, 0, 'ModalRelacion_crearPersona', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(101,'Aceptar Agregar Relación', 'BTN', 96, 2, 152, 'ModalRelacion_aceptar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(102,'Aceptar Modificar Relación', 'BTN', 97, 1, 153, 'ModalRelacion_aceptarMod', 'S');


--Pestaña Nombres Fantasía de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(103,'Pestaña Nombres Fantasía', 'BTN', 43, 10, 0, 'TabPersonaTabNombresFantasia', 'S');
--Botones en pestaña Nombres Fantasía de la pestaña PERSONA
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(104,'Agregar Nombre Fantasía', 'BTN', 103, 1, 0, 'PanelPersonaNombresFantasia_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(105,'Bloquear Nombre Fantasía', 'BTN', 103, 2, 165, 'PanelPersonaNombresFantasia_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(106,'Desbloquear Nombre Fantasía', 'BTN', 103, 3, 165, 'PanelPersonaNombresFantasia_desbloquear', 'S');
--Botones en pantalla Modal Nombre Fantasía
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(107,'Aceptar Agregar Nombre Fantasía', 'BTN', 104, 1, 164, 'ModalNombresFantasia_aceptar', 'S');


---------------------------------------------------------------------------------------------
--------------------------------Pestaña CLIENTE----------------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(108,'Pestaña Cliente', 'BTN', 41, 3, 0, 'TabCliente', 'S');


--Pestaña Datos Generales de la pestaña CLIENTE
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(109,'Pestaña Datos Generales', 'BTN', 108, 1, 0, 'TabClienteTabDatosGenerales', 'S');
--Botones en pestaña Datos Generales de la pestaña CLIENTE
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(110,'Editar Datos Generales', 'BTN', 109, 1, 0, 'PanelClienteDatosGenerales_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(111,'Guardar Datos Generales', 'BTN', 110, 2, 95, 'PanelClienteDatosGenerales_guardar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(112,'Bloquear Datos Generales', 'BTN', 109, 3, 0, 'PanelClienteDatosGenerales_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(113,'Desbloquear Datos Generales', 'BTN', 109, 4, 96, 'PanelClienteDatosGenerales_desbloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(114,'Solicitar Autorización Enrolamiento', 'BTN', 109, 5, 130, 'PanelclienteDatosGenerales_solAutEnrol', 'S');
--Botones en pantalla Modal Cliente Bloqueo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(115,'Guardar Bloqueo Cliente', 'BTN', 112, 1, 96, 'ModalClienteBloqueo_guardar', 'S');


--Pestaña Portafolios Cliente de la pestaña CLIENTE
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(116,'Pestaña Portafolios Cliente', 'BTN', 108, 2, 0, 'TabClienteTabPortafoliosCliente', 'S');
--Botones en pestaña Portafolios Cliente de la pestaña CLIENTE
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(117,'Nuevo Portafolio Cliente', 'BTN', 116, 1, 0, 'PanelClientePortafolios_crearPortafolio', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(118,'Guardar Portafolio Cliente', 'BTN', 117, 1, 59, 'AdministrarPortafolioDatosGenerales_guardar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(201,'Activar Portafolio Cliente', 'BTN', 116, 2, 0, 'PanelClientePortafolios_Bloq_Act', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(202,'Bloquear Portafolio Cliente', 'BTN', 116, 3, 0, 'PanelClientePortafolios_Bloq_Blo', 'S');

----------------------------------------------------------------------------------------------------
--------------------------------Administrar Portafolio----------------------------------------------
----------------------------------------------------------------------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(119,'Administrar Portafolio Cliente', 'BTN', 116, 4, 0, 'AdministrarPortafolio', 'S');

--Pestaña Datos Generales de Administrar Portafolio
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(120,'Pestaña Datos Generales', 'BTN', 119, 1, 0, 'TabDatosGenerales', 'S');
--Botones en pestaña Datos Generales de Administrar Portafolio
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(121,'Editar Datos Generales', 'BTN', 120, 1, 0, 'AdministrarPortafolioDatosGenerales_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(122,'Guardar Datos Generales', 'BTN', 121, 1, 59, 'AdministrarPortafolioDatosGenerales_guardarMod', 'S');

----------------------------------------------------------------------------------------------------
--------------------------------Pestaña Perfil Comercial--------------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(123,'Pestaña Perfil Comercial', 'BTN', 119, 2, 0, 'TabPerfilComercial', 'S');

--Pestaña Matriz Operacional en pestaña Perfil Comercial
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(124,'Pestaña Matriz Operacional', 'BTN', 123, 1, 0, 'AdmPortPerfCom_MatrizOperacional', 'S');
--Botones en pestaña Matriz Operacional
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(125,'Editar Matriz Operacional', 'BTN', 124, 1, 0, 'AdmPortPerfCom_MatrizOperacional_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(126,'Guardar Matriz Operacional', 'BTN', 125, 1, 57, 'AdmPortPerfCom_MatrizOperacional_guardar', 'S');


--Pestaña Perfil de Productos en pestaña Perfil Comercial
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(127,'Pestaña Perfil de Productos', 'BTN', 123, 2, 0, 'AdmPortPerfCom_PerfilProductos', 'S');
--Botones en pestaña Perfil de Productos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(128,'Personalizar Perfil Productos', 'BTN', 127, 1, 0, 'AdmPortPerfCom_PerfilProductos_personalizar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(129,'Editar Perfil Productos', 'BTN', 127, 2, 0, 'AdmPortPerfCom_PerfilProductos_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(130,'Guardar Perfil Productos', 'BTN', 127, 3, 57, 'AdmPortPerfCom_PerfilProductos_guardar', 'S');


--Pestaña Perfil de Cobros en pestaña Perfil Comercial
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(131,'Pestaña Perfil de Cobros', 'BTN', 123, 3, 0, 'AdmPortPerfCom_PerfilCobros', 'S');
--Botones en pestaña Perfil de Cobros
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(132,'Personalizar Perfil Cobros', 'BTN', 131, 1, 0, 'AdmPortPerfCom_PerfilCobros_personalizar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(133,'Editar Perfil Cobros', 'BTN', 131, 2, 0, 'AdmPortPerfCom_PerfilCobros_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(134,'Guardar Perfil Cobros', 'BTN', 131, 3, 57, 'AdmPortPerfCom_PerfilCobros_guardar', 'S');


--Pestaña Perfil de Facturación en pestaña Perfil Comercial
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(135,'Pestaña Perfil de Facturación', 'BTN', 123, 4, 0, 'AdmPortPerfCom_PerfilFacturacion', 'S');


--Pestaña Perfil de Condiciones Operativas en pestaña Perfil Comercial
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(136,'Pestaña Perfil de Condiciones Operativas', 'BTN', 123, 5, 0, 'AdmPortPerfCom_PerfilCondicionesOperativas', 'S');
--Botones en pestaña Perfil de Condiciones Operativas
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(137,'Personalizar Perfil Condiciones Operativas', 'BTN', 136, 1, 0, 'AdmPortPerfCom_PerfilCondOper_personalizar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(138,'Editar Perfil Condiciones Operativas', 'BTN', 136, 2, 0, 'AdmPortPerfCom_PerfilCondOper_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(139,'Guardar Perfil Condiciones Operativas', 'BTN', 136, 3, 57, 'AdmPortPerfCom_PerfilCondOper_guardar', 'S');


---------------------------------------------------------------------------------------------
--------------------------------Pestaña Datos Uso--------------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(140,'Pestaña Datos Uso', 'BTN', 119, 3, 0, 'TabDatosUso', 'S');
--Botones en pestaña Datos Uso de Administrar Portafolio
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(141,'Editar Datos Uso', 'BTN', 140, 1, 0, 'AdministrarPortafolioDatosUso_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(142,'Guardar Datos Uso', 'BTN', 141, 1, 125, 'AdministrarPortafolioDatosUso_guardar', 'S');


---------------------------------------------------------------------------------------------
--------------------------------Pestaña Atributos--------------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(143,'Pestaña Atributos', 'BTN', 119, 4, 0, 'TabAtributos', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(144,'Pantalla Atributos Portafolio', 'BTN', 143, 1, 0, 'AdministrarPortafolioAtributos', 'S');


---------------------------------------------------------------------------------------------
--------------------------------Pestaña Preferencias-----------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(145,'Pestaña Preferencias', 'BTN', 119, 5, 0, 'TabPreferencias', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(146,'Pantalla Preferencias Portafolio', 'BTN', 145, 1, 0, 'AdministrarPortafolioPreferencias', 'S');


---------------------------------------------------------------------------------------------
--------------------------------Pestaña Autorizaciones-----------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(147,'Pestaña Autorizaciones', 'BTN', 119, 6, 0, 'TabAutorizaciones', 'S');
--Botones en pestaña Autorizaciones de Administrar Portafolio
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(148,'Agregar Autorización', 'BTN', 147, 1, 0, 'AdministrarPortafolioAutorizaciones_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(149,'Bloquear Autorización', 'BTN', 147, 2, 162, 'AdministrarPortafolioAutorizaciones_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(150,'Desbloquear Autorización', 'BTN', 147, 3, 162, 'AdministrarPortafolioAutorizaciones_desbloquear', 'S');
--Botones en pantalla Modal Autorizacion
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(151,'Aceptar Agregar Autorización', 'BTN', 148, 1, 156, 'AdministrarPortafolioAutorizacionesModalCrear_aceptar', 'S');

----------------------------------------------------------------------------------------------------
--------------------------------FIN Administrar Portafolio------------------------------------------
----------------------------------------------------------------------------------------------------


--Pestaña Atributos de la pestaña CLIENTE
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(152,'Pestaña Atributos', 'BTN', 108, 3, 0, 'TabClienteTabAtributos', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(153,'Pantalla Atributos Cliente', 'BTN', 152, 1, 0, 'PanelClienteAtributos', 'S');


--Pestaña Documentos de la pestaña CLIENTE
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(154,'Pestaña Documentos', 'BTN', 108, 4, 0, 'TabClienteTabDocumentos', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(155,'Pantalla Modal Cliente Firmar Anexo', 'BTN', 154, 1, 0, 'ModalClienteFirmaAnexo', 'S');
--Botones en pantalla Modal Cliente Firmar Anexo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(156,'Guardar Firmar Anexo', 'BTN', 155, 1, 73, 'ModalClienteFirmaAnexo_guardar', 'S');


---------------------------------------------------------------------------------------------
--------------------------------Pestaña EMPLEADO----------------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(157,'Pestaña Empleado', 'BTN', 41, 4, 0, 'TabEmpleado', 'S');


--Pestaña Datos Generales de la pestaña EMPLEADO
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(158,'Pestaña Datos Generales', 'BTN', 157, 1, 0, 'TabEmpleadoTabDatosGenerales', 'S');
--Botones en pestaña Datos Generales de la pestaña EMPLEADO
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(159,'Editar Datos Generales', 'BTN', 158, 1, 0, 'PanelEmpleadoDatosGenerales_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(160,'Guardar Datos Generales', 'BTN', 159, 1, 98, 'PanelEmpleadoDatosGenerales_guardar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(161,'Bloquear Datos Generales', 'BTN', 158, 2, 0, 'PanelEmpleadoDatosGenerales_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(162,'Desbloquear Datos Generales', 'BTN', 158, 3, 0, 'PanelEmpleadoDatosGenerales_desbloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(163,'Solicitar Autorización Enrolamiento', 'BTN', 158, 4, 130, 'PanelEmpleadoDatosGenerales_solAutEnrol', 'S');
--Botones en pantalla Modal Empleado Bloqueo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(164,'Guardar Bloqueo Empleado', 'BTN', 161, 1, 103, 'ModalEmpleadoBloqueo_guardarBloqueo', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(165,'Guardar Desbloqueo Empleado', 'BTN', 162, 1, 103, 'ModalEmpleadoBloqueo_guardarDesbloqueo', 'S');


--Pestaña Atributos de la pestaña EMPLEADO
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(166,'Pestaña Atributos', 'BTN', 157, 2, 0, 'TabEmpleadoTabAtributos', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(167,'Pantalla Atributos Empleado', 'BTN', 166, 1, 0, 'PanelEmpleadoAtributos', 'S');


---------------------------------------------------------------------------------------------
--------------------------------Pestaña CONTRAPARTE----------------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(168,'Pestaña Contraparte', 'BTN', 41, 5, 0, 'TabContraparte', 'S');


--Pestaña Datos Generales de la pestaña CONTRAPARTE
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(169,'Pestaña Datos Generales', 'BTN', 168, 1, 0, 'TabContraparteTabDatosGenerales', 'S');
--Botones en pestaña Datos Generales de la pestaña CONTRAPARTE
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(170,'Editar Datos Generales', 'BTN', 169, 1, 0, 'PanelContraparteDatosGenerales_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(171,'Guardar Datos Generales', 'BTN', 170, 1, 110, 'PanelContraparteDatosGenerales_guardar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(172,'Bloquear Datos Generales', 'BTN', 169, 2, 0, 'PanelContraparteDatosGenerales_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(173,'Desbloquear Datos Generales', 'BTN', 169, 3, 0, 'PanelContraparteDatosGenerales_desbloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(174,'Solicitar Autorización Enrolamiento', 'BTN', 169, 4, 130, 'PanelContraparteDatosGenerales_solAutEnrol', 'S');
--Botones en pantalla Modal Contraparte Bloqueo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(175,'Guardar Bloqueo Contraparte', 'BTN', 172, 1, 111, 'ModalContraparteBloqueo_guardarBloqueo', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(176,'Guardar Desbloqueo Contraparte', 'BTN', 173, 1, 111, 'ModalContraparteBloqueo_guardarDesbloqueo', 'S');


---------------------------------------------------------------------------------------------
--------------------------------Pestaña PROVEEDOR--------------------------------------------
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(177,'Pestaña Proveedor', 'BTN', 41, 6, 0, 'TabProveedor', 'S');

--Pestaña Datos Generales de la pestaña PROVEEDOR
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(178,'Pestaña Datos Generales', 'BTN', 177, 1, 0, 'TabProveedorTabDatosGenerales', 'S');
--Botones en pestaña Datos Generales de la pestaña PROVEEDOR
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(179,'Editar Datos Generales', 'BTN', 178, 1, 0, 'PanelProveedorDatosGenerales_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(180,'Guardar Datos Generales', 'BTN', 179, 1, 118, 'PanelProveedorDatosGenerales_guardar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(181,'Bloquear Datos Generales', 'BTN', 178, 2, 0, 'PanelProveedorDatosGenerales_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(182,'Desbloquear Datos Generales', 'BTN', 178, 3, 0, 'PanelProveedorDatosGenerales_desbloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(183,'Solicitar Autorización Enrolamiento', 'BTN', 178, 4, 130, 'PanelProveedorDatosGenerales_solAutEnrol', 'S');
--Botones en pantalla Modal Proveedor Bloqueo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(184,'Guardar Bloqueo Proveedor', 'BTN', 181, 1, 119, 'ModalProveedorBloqueo_guardarBloqueo', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(185,'Guardar Desbloqueo Proveedor', 'BTN', 182, 1, 119, 'ModalProveedorBloqueo_guardarDesbloqueo', 'S');


--Pestaña Atributos de la pestaña PROVEEDOR
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(186,'Pestaña Atributos', 'BTN', 177, 2, 0, 'TabProveedorTabAtributos', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(187,'Pantalla Atributos Proveedor', 'BTN', 186, 1, 0, 'PanelProveedorAtributos', 'S');

---------------------------FIN Submenú Socios de Negocio--------------------------------------------
----------------------------------------------------------------------------------------------------


--Submenú Crear Persona
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(189,'Pantalla Crear Persona', 'MENU2', 2, 3, 0, 'CrearPersona', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(190,'Botón Crear Persona', 'BTN', 189, 1, 90, 'formCrearPersona_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(191,'Botón Guardar Borrador Persona', 'BTN', 189, 2, 52, 'formCrearPersona_guardarBorrador', 'S');


--Submenú Crear Empleado
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(192,'Pantalla Crear Empleado', 'MENU2', 2, 4, 0, 'CrearEmpleado', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(193,'Botón Crear Empleado', 'BTN', 192, 1, 101, 'formCrearEmpleado_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(194,'Botón Guardar Borrador Empleado', 'BTN', 192, 2, 52, 'formCrearEmpleado_guardarBorrador', 'S');


--Submenú Crear Contraparte
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(195,'Pantalla Crear Contraparte', 'MENU2', 2, 5, 0, 'CrearContraparte', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(196,'Botón Crear Contraparte', 'BTN', 195, 1, 104, 'formCrearContraparte_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(197,'Botón Guardar Borrador Contraparte', 'BTN', 195, 2, 52, 'formCrearContraparte_guardarBorrador', 'S');


--Submenú Crear Proveedor
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(198,'Pantalla Crear Proveedor', 'MENU2', 2, 6, 0, 'CrearProveedor', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(199,'Botón Crear Proveedor', 'BTN', 198, 1, 117, 'formCrearProveedor_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(200,'Botón Guardar Borrador Proveedor', 'BTN', 198, 2, 52, 'formCrearProveedor_guardarBorrador', 'S');


----------------------------------------------------------------------------------------------------
--------------------------- PLATAFORMA - Administrar Plantillas ------------------------------------
----------------------------------------------------------------------------------------------------

INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(203,'Administrar Plantillas', 'MENU2', 13, 2, 0, 'PerfilCOM', 'S');

--Pestaña Plantilla Comercial
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(204,'Pestaña Plantilla Comercial', 'BTN', 203, 1, 0, 'AdmPla_PlantillaComercial', 'S');
--Botones en pestaña Plantilla Comercial
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(205,'Editar Plantilla Comercial', 'BTN', 204, 1, 0, 'AdmPla_PlantillaComercial_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(206,'Crear Plantilla Comercial', 'BTN', 204, 2, 0, 'AdmPla_PlantillaComercial_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(207,'Guardar Plantilla Comercial', 'BTN', 204, 3, 46, 'AdmPla_PlantillaComercial_guardar', 'S');


--Pestaña Plantilla Productos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(208,'Pestaña Plantilla Productos', 'BTN', 203, 2, 0, 'AdmPla_PlantillaProductos', 'S');
--Botones en pestaña Plantilla Productos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(209,'Editar Plantilla Productos', 'BTN', 208, 1, 0, 'AdmPla_PlantillaProductos_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(210,'Crear Plantilla Productos', 'BTN', 208, 2, 0, 'AdmPla_PlantillaProductos_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(211,'Guardar Plantilla Productos', 'BTN', 208, 3, 45, 'AdmPla_PlantillaProductos_guardar', 'S');


--Pestaña Plantilla Cobros
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(212,'Pestaña Plantilla Cobros', 'BTN', 203, 3, 0, 'AdmPla_PlantillaCobros', 'S');
--Botones en pestaña Plantilla Cobros
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(213,'Editar Plantilla Cobros', 'BTN', 212, 1, 0, 'AdmPla_PlantillaCobros_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(214,'Crear Plantilla Cobros', 'BTN', 212, 2, 0, 'AdmPla_PlantillaCobros_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(215,'Guardar Plantilla Cobros', 'BTN', 212, 3, 44, 'AdmPla_PlantillaCobros_guardar', 'S');


--Pestaña Plantilla Facturación
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(216,'Pestaña Plantilla Facturación', 'BTN', 203, 4, 0, 'AdmPla_PlantillaFacturacion', 'S');


--Pestaña Plantilla Condiciones Operativas
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(217,'Pestaña Plantilla Condiciones Operativas', 'BTN', 203, 5, 0, 'AdmPla_PlantillaCondicionesOperativas', 'S');
--Botones en pestaña Plantilla Condiciones Operativas
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(218,'Editar Plantilla Condiciones Operativas', 'BTN', 217, 1, 0, 'AdmPla_PlantillaCondicionesOperativas_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(219,'Crear Plantilla Condiciones Operativas', 'BTN', 217, 2, 0, 'AdmPla_PlantillaCondicionesOperativas_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(220,'Guardar Plantilla Condiciones Operativas', 'BTN', 217, 3, 136, 'AdmPla_PlantillaCondicionesOperativas_guardar', 'S');


--Pestaña Grupo de Activos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(221,'Pestaña Grupo de Activos', 'BTN', 203, 6, 0, 'AdmPla_GrupoDeActivos', 'S');
--Botones en pestaña Grupo de Activos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(222,'Editar Grupo de Activos', 'BTN', 221, 1, 0, 'AdmPla_GrupoDeActivos_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(223,'Crear Grupo de Activos', 'BTN', 221, 2, 0, 'AdmPla_GrupoDeActivos_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(224,'Guardar Grupo de Activos', 'BTN', 221, 3, 97, 'AdmPla_GrupoDeActivos_guardar', 'S');


--Pestaña Plantilla Empleado
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(225,'Pestaña Plantilla Empleado', 'BTN', 203, 7, 0, 'AdmPla_PlantillaEmpleado', 'S');
--Botones en pestaña Plantilla Empleado
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(226,'Editar Plantilla Empleado', 'BTN', 225, 1, 0, 'AdmPla_PlantillaEmpleado_editar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(227,'Crear Plantilla Empleado', 'BTN', 225, 2, 0, 'AdmPla_PlantillaEmpleado_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(228,'Guardar Plantilla Empleado', 'BTN', 225, 3, 143, 'AdmPla_PlantillaEmpleado_guardar', 'S');


----------------------------------------------------------------------------------------------------
--------------------------- PLATAFORMA - Mantenedor Atributos ------------------------------------
----------------------------------------------------------------------------------------------------

INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(229,'Mantenedor Atributos', 'MENU2', 13, 3, 0, 'MantenedorAtributos', 'S');
--Botones en pantalla Mantenedor Atributos - Detalle Atributos
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(230,'Crear Detalle Atributo', 'BTN', 229, 1, 0, 'MantenedorAtributos_crearDetalle', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(231,'Modificar Detalle Atributo', 'BTN', 229, 2, 0, 'MantenedorAtributos_modificarDetalle', 'S');
--Pantalla Modal Detalle Atributo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(232,'Botón Aceptar Crear Detalle Atributo', 'BTN', 230, 1, 134, 'ModalDetalleAtributo_aceptarCrear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(233,'Botón Aceptar Modificar Detalle Atributo', 'BTN', 231, 1, 134, 'ModalDetalleAtributo_aceptarModificar', 'S');
--Botones en pantalla Mantenedor Atributos - Tipo Atributo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(234,'Crear Tipo Atributo', 'BTN', 229, 3, 0, 'MantenedorAtributos_crearTipoAtr', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(235,'Modificar Tipo Atributo', 'BTN', 229, 4, 0, 'MantenedorAtributos_modificarTipoAtr', 'S');
--Pantalla Modal Tipo Atributo
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(236,'Botón Aceptar Crear Tipo Atributo', 'BTN', 234, 1, 122, 'ModalTipoAtributo_aceptarCrear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(237,'Botón Aceptar Modificar Tipo Atributo', 'BTN', 235, 1, 122, 'ModalTipoAtributo_aceptarModificar', 'S');


----------------------------------------------------------------------------------------------------
--------------------------- PLATAFORMA - Mantenedor Tipo Relaciones ------------------------------------
----------------------------------------------------------------------------------------------------

INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(238,'Mantenedor Tipo Relaciones', 'MENU2', 13, 4, 0, 'MantenedorTipoRelaciones', 'S');
--Botones en pantalla Mantenedor Tipo Relaciones
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(239,'Agregar Tipo Relación', 'BTN', 238, 1, 0, 'MantenedorTipoRelaciones_agregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(240,'Modificar Tipo Relación', 'BTN', 238, 2, 0, 'MantenedorTipoRelaciones_modificar', 'S');
--Botones en pantalla Modal Tipo Relacion
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(241,'Botón Aceptar Agregar Tipo Relación', 'BTN', 239, 1, 180, 'ModalTipoRelacion_aceptarAgregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(242,'Botón Aceptar Modificar Tipo Relación', 'BTN', 240, 1, 181, 'ModalTipoRelacion_aceptarModificar', 'S');


--Mantenedor Grupos clientes
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(243,'Consultar Grupo Clientes', 'MENU2', 3, 10, 0, 'ConsultarGruposPortafolio', 'S');

INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(244,'Pantalla Crear Grupo', 'BTN', 243, 1, 0, 'ConsultaGruposPortafolio_crearGrupo', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(245,'Pantalla Modificar Grupo', 'BTN', 243, 2, 0, 'ConsultaGruposPortafolio_modificarGrupo', 'S');

INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(246,'BTN Crear Grupo', 'BTN', 244, 1, 195, 'CrearGrupoPortafolio_crearGrupo', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(247,'BTN GUARDAR', 'BTN', 245, 1, 200, 'ModificarGrupo_actualizarGrupo', 'S');

--Accesos pantalla modal Bloqueo-Desbloqueo de Socios de Negocio
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(248,'Botón Aceptar Bloquear Datos Generales', 'BTN', 47, 1, 108, 'ModalSocioNegocioBloqueo_PERSONA_AceptarBLO', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(249,'Botón Aceptar Desbloquear Datos Generales', 'BTN', 48, 1, 108, 'ModalSocioNegocioBloqueo_PERSONA_AceptarACT', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(250,'Botón Aceptar Desbloquear Datos Generales', 'BTN', 113, 1, 96, 'ModalSocioNegocioBloqueo_CLIENTE_AceptarACT', 'S');


----------------------------------------------------------------------------------------------------
----------------------- OPERACIONES EN RUEDA - Maestro Libro Ordenes -------------------------------
----------------------------------------------------------------------------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(251,'Maestro Libro Órdenes', 'MENU2', 3, 11, 0, 'MaestroLibroOrdenes', 'S');
--Botones en pantalla Maestro Libro Ordenes
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(252,'Pantalla Agregar Libro Órdenes', 'BTN', 251, 1, 0, 'MaestroLibroOrdenes_crear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(253,'Pantalla Modificar Libro Órdenes', 'BTN', 251, 2, 0, 'MaestroLibroOrdenes_modificar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(254,'Botón Eliminar Libro Órdenes', 'BTN', 251, 3, 213, 'MaestroLibroOrdenes_eliminar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(255,'Botón Bloquear Libro Órdenes', 'BTN', 251, 4, 213, 'MaestroLibroOrdenes_bloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(256,'Botón Desbloquear Libro Órdenes', 'BTN', 251, 5, 213, 'MaestroLibroOrdenes_desbloquear', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(257,'Pantalla Libro Por Defecto', 'BTN', 251, 6, 0, 'MaestroLibroOrdenes_libroDef', 'S');
-- Botones en pantalla Maestro Libro Ordenes - SubGrillas Ope, Seg, MarcasNeg
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(258,'Grilla Operadores', 'BTN', 251, 7, 0, 'MaestroLibroOrdenes_agregarOperador', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(259,'Grilla Segmento', 'BTN', 251, 8, 0, 'MaestroLibroOrdenes_agregarSegmento', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(260,'Grilla Marcas Negociación', 'BTN', 251, 9, 0, 'MaestroLibroOrdenes_agregarMarcasNegociacion', 'S');
--Botones en pantalla Modal Libro Ordenes
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(261,'Botón Aceptar Agregar Libro Órdenes', 'BTN', 252, 1, 211, 'ModalLibroOrdenes_aceptarAgregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(262,'Botón Aceptar Modificar Libro Órdenes', 'BTN', 253, 1, 212, 'ModalLibroOrdenes_aceptarModificar', 'S');
--Botones en pantalla Modal de SubGrillas Ope, Seg, MarcasNeg
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(263,'Botón Agregar Operadores', 'BTN', 258, 1, 217, 'ModalLibroOrdenesOperadores_aceptarAgregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(264,'Botón Eliminar Operadores', 'BTN', 258, 2, 220, 'MaestroLibroOrdenes_eliminarOperador', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(265,'Botón Agregar Segmento', 'BTN', 259, 1, 221, 'ModalLibroOrdenesSegmento_aceptarAgregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(266,'Botón Eliminar Segmento', 'BTN', 259, 2, 220, 'MaestroLibroOrdenes_eliminarSegmento', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(267,'Botón Agregar Marcas Negociación', 'BTN', 260, 1, 222, 'ModalLibroOrdenesMarcasNegociacion_aceptarAgregar', 'S');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(268,'Botón Eliminar Marcas Negociación', 'BTN', 260, 2, 220, 'MaestroLibroOrdenes_eliminarMarcasNegociacion', 'S');
--Botones en pantalla Modal Libro Por Defecto
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(269,'Botón Aceptar Libro Por Defecto', 'BTN', 257, 1, 223, 'ModalLibroPorDefecto_aceptar', 'S');

--Botones pantalla cuadratura y cierre diario
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(270,'UI_CUADRATURA_CIERRE_DIARIO', 'MENU2', 3, 12, 0, 'CuadraturaCierreDiario', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(271,'UI_CUADRAR_SISTEMA', 'BTN', 270, 1, 244, 'CuadraturaCierreDiario_Cuadrar', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(272,'UI_CERRAR_SISTEMA', 'BTN', 270, 1, 263, 'CuadraturaCierreDiario_Cerrar', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(273,'UI_OFICIALIZAR_ORDENES', 'BTN', 270, 3, 276, 'CuadraturaCierreDiario_OficializarOrdenes', 'S');


----------------------------------------------------------------------------------------------------
------------------------------------- PLATAFORMA - AUTORIZACIONES ----------------------------------
----------------------------------------------------------------------------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(275,'Autorizaciones', 'MENU2', 13, 5, 0, 'Autorizaciones', 'S');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(276,'Servicio Crear Autorizado Etapa', 'BTN', 275, 1, 50, 'EtapasAutorizador_crear', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(277,'Servicio Autorizar', 'BTN', 275, 2, 41, 'VistaAutorizaciones_Autorizar', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(278,'Servicio Rechazar', 'BTN', 275, 3, 41, 'VistaAutorizaciones_Rechazar', 'S');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(301,'Bloqueo Masivo Clientes', 'MENU2', 2, 11, 0, 'BloqueoMasivoClientes', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(302,'Consultar Contratos', 'MENU2', 2, 12, 0, 'ImprimirArchivosContratos', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(303,'Mantenedor Datos Contratos', 'MENU2', 13, 7, 0, 'MantenedorDatosContratos', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(304,'Mantenedor Contratos', 'MENU2', 13, 8, 0, 'MantenedorArchivosContratos', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(305,'Consultar Asuntos Pendientes', 'MENU2', 13, 9, 0, 'ConsultarAsuntosPendientes', 'S');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(306,'Consultar Accesos De Usuarios', 'MENU2', 13, 10, 0, 'ConsultarAccesosUsuarios', 'S');


--ACCESOS
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"
VALUES(1, 'PLA.AccesosPerfilesRepetidos', 'Configura si dos perfiles pueden tener los mismos accesos', 'N');




----------------------------ACCESOS_SERVICIOS_SISTEMA--------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100001,'Servicio Tiempo', 'SERV', 0, 0, 14, 'SERVICIO_TIEMPO', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100002,'Busqueda instrumentos', 'SERV', 0, 0, 17, 'BOLSA_INSTRUMENTO_PRECIOS', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100003,'Consultar fecha liquidacion', 'SERV', 0, 0, 1, 'CONSULTA_FECHA_LIQUIDACION', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100004,'Ingreso manual de titulos', 'SERV', 0, 0, 3, 'INGRESO_MANUAL_TITULOS', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100005,'Consultar flujo instrumento', 'SERV', 0, 0, 19, 'CONSULTAR_FLUJO_INSTRUMENTO', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100006,'Certificacion masiva Movimientos en transito', 'SERV', 0, 0, 11, 'CERTIFICA_MASIVO_MOVTOS_EN_TRANSITO', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100007,'Consultar dia siguiente', 'SERV', 0, 0, 23, 'CONSULTA_DIA_SIGUIENTE', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100008,'Servicio Cerrar Sesion', 'SERV', 0, 0, 182, 'SERVICIO_CERRAR_SESION', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100009,'Servicio Cambio Clave', 'SERV', 0, 0, 184, 'SERVICIO_CAMBIO_CLAVE', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100010,'Valorizacion RV', 'SERV', 0, 0, 194, 'VALORIZACION_RV', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100011,'Valorizacion RF-IF', 'SERV', 0, 0, 196, 'VALORIZACION_RF', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100012,'Valorizacion IF', 'SERV', 0, 0, 197, 'VALORIZACION_IF', 'N');



INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Cartola Movimientos Custodia','SERV', 0, 0, 291, 'CARTOLA_MOVIMIENTOS', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Anular Movimiento en Transito','SERV', 0, 0, 15, 'ANULA_MOVIMIENTO_TRANSITO', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Consulta custodia portafolio','SERV', 0, 0, 22, 'CONSULTA_CUSTODIA_PORTAFOLIO', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Consultar plazo','SERV', 0, 0, 60, 'CONSULTAR_PLAZO', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";


INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Servicio valorizacion Masivo','SERV', 0, 0, 266, 'VALORIZAR_RF_MASIVO', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"
("IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
SELECT (MAX("IdAcceso")+1),'Servicio Fecha Vencimiento','SERV', 0, 0, 47, 'CONSULTA_FECHA_VENCIMIENTO', 'N' From "BSCL"."BSCL.Plataforma.BD::ACC.Acceso";


----------------------------------------------------------------------------------------------------
------------------------------------- PLATAFORMA - CONSULTA LOGS -----------------------------------
----------------------------------------------------------------------------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(285,'Consulta Logs', 'MENU2', 13, 6, 0, 'ConsultaLog', 'S');

----------------------------------------------------------------------------------------------------

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion")
VALUES(1013, 'BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda', 3, 'Q', 'Plataforma');



/*==============================================================*/
/* FIN Insert Table ACCESOS                                     */
/*==============================================================*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/


commit;
