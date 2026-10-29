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
-- LIBERACION SEMANA: 19-02-2016
--

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (70,'USD','GEN','','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (71,'EUR','GEN','','',4,'T',4,'T',4,'T',4,'T',4,'T',4,'T',4,'T','ACT');

-----------------------------------CREACION MENU DINAMICO--------------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'UsuarioAdministrador', 'Usuario con todos los accesos por default', 'MASTER');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'ConflictoAccesoDefecto', 'Decide si en conflicto de accesos manda el habilitado o no', 'S');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(179,'BSCL.Orden.BD.Servicios::ReservarLiberarTransacciones',0,'P','');
--------------------------MODIFICACIONES CREAR SESION------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(182,'BSCL.Plataforma.BD.Servicios::ACC.CerrarSesionUsuario',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100008,'Servicio Cerrar Sesion', 'SERV', 0, 0, 182, 'SERVICIO_CERRAR_SESION', 'N');
--------------------------CAMBIAR CLAVE USUARIO------------------------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(184,'BSCL.Plataforma.BD.Servicios::ACC.CambiarClaveUsuario',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100009,'Servicio Cambio Clave', 'SERV', 0, 0, 184, 'SERVICIO_CAMBIO_CLAVE', 'N');
---------------------------------- SERVICIOS NECESARIOS PARA CIERRE DE SESION------------------------
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(182,'BSCL.Plataforma.BD.Servicios::ACC.CerrarSesionUsuario',0,'P','');
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso"( "IdAcceso","NombreAcceso","TipoAcceso", "Padre","Orden","IdServicio", "IdElementoHtml", "EsVisibleMenu")
VALUES(100008,'Servicio Cerrar Sesion', 'SERV', 0, 0, 182, 'SERVICIO_CERRAR_SESION', 'N');


---------------------------------- INCORPORACION MODEDA LIQUIDACION PARA FUTUROS DOLAR --------
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion") 
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+1,'FDO','USD','CLP' FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" UNION ALL
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+2,'FBC','USD','CLP'  FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" 


---------------------------------- CAMPOS INSTRUMENTOS PARA NORMATIVA ------------------------
-- Modificacion de estructura -> Agregar campo
alter table "BSCL.Plataforma.BD::INS.Instrumento" add ("TasaFiscal" DECIMAL(26,8)   default '0' not null);
alter table "BSCL.Plataforma.BD::INS.Instrumento" add ("IndArt104"  NVARCHAR(1) 	default 'N' not null);
	 


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (72,'CLP','ORD','FIP','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (73,'CLP','ORD','FDO','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (74,'CLP','ORD','FBC','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (75,'CLP','ORD','FIC','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (76,'CLP','ORD','FUF','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (77,'USD','ORD','FDO','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (78,'UF','ORD','FUF','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (79,'USD','ORD','FBC','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (80,'CLP','ORD','PAG','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (81,'USD','ORD','PAG','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (82,'EUR','ORD','PAG','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (83,'USD','ORD','CFX','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.MonedaFormato" ("IdMonedaFormato","IdMoneda","IdUsoMonedaFormato","IdTipoInstrumento","IdTipoOperacion","PrecioDecimales","PrecioTrucaRedondea","TasaDecimales","TasaTrucaRedondea","CantidadDecimales",  "CantidadTrucaRedondea","MontoDecimales","MontoTrucaRedondea","TipoCambioDecimales","TipoCambioTrucaRedondea","ParidadDecimales","ParidadTrucaRedondea","PuntosDecimales","PuntosTrucaRedondea","IdEstado") 
VALUES (85,'EUR','ORD','CFX','',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T',2,'T','ACT');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(186,'BSCL.Orden.BD.Servicios::ObtenerDetalleSimulacionAsignacion',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ( "IdClasificacionTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('SP','SPT','Spot','Spot','ACT');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('DOL','DOLAR','DOLAR','DOLAR','VIG','RV');


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CAEPER','','SOC','CAMBIO ESTADO PERSONA','VIG');


alter table "BSCL.Plataforma.BD::PLA.TipoAtributo" add ("EsPreferencia" NVARCHAR(1) DEFAULT 'N' NOT NULL);

--TIPO DATO para combobox en mantenedor atributos

create column table "BSCL.Plataforma.BD::PLA.TipoDatoTipoAtributo" (
   "IdTipoDatoTipoAtributo" BIGINT               not null,
   "TipoDato"      			NVARCHAR(20)         not null,
   "NombreTipoDato"			NVARCHAR(20)		 not null,
   primary key  ("IdTipoDatoTipoAtributo")
);

insert into "BSCL"."BSCL.Plataforma.BD::PLA.TipoDatoTipoAtributo" values(1,'COMBO','Combobox');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.TipoDatoTipoAtributo" values(2,'BIGINT','Numérico');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.TipoDatoTipoAtributo" values(3,'NVARCHAR','String');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.TipoDatoTipoAtributo" values(4,'DATE','Fecha');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(188,'BSCL.Orden.BD.Servicios::CrearLoteAsignacion',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(190,'BSCL.Mercado.BD.Servicios::ValorizarRVMasivo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(191,'BSCL.Orden.BD.Servicios::CancelarSimulacionAsignacion',0,'P','');

--################################################
--       SERVICIOS CONFIGURACION CHEQUERA
--################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(185,'BSCL.Tesoreria.BD.Servicios::CrearConfiguracionChequera',0,'P','');
		
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(189,'BSCL.Tesoreria.BD.Servicios::ModificarConfiguracionChequera',0,'P','');
--------------------------------------------------
--      FIN SERVICIOS CONFIGURACION CHEQUERAS
--------------------------------------------------

--################################################
--       			LOG
--################################################
alter table "BSCL.Plataforma.BD::PLA.Log" alter ("Cantidad" DECIMAL(26,8) CS_FIXED);
alter table "BSCL.Plataforma.BD::PLA.Log" alter ("PrecioTasa" DECIMAL(26,8) CS_FIXED);
alter table "BSCL.Plataforma.BD::PLA.Log" alter ("Monto" DECIMAL(26,8) CS_FIXED);

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoLog" ( "IdTipoLog","Descripcion" )  
VALUES('ANULACION','ERRORES O INFORMATIVO POR ANULACION DE DATOS');
--------------------------------------------------
--      		FIN LOG
--------------------------------------------------



--################################################
--       	Inscripcion de Accesos
--################################################

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

--################################################
--       	FIN Inscripcion de Accesos
--################################################



--################################################
--       JG.ELIMINAR DEPRECADOS COM
--################################################
SET SCHEMA "BSCL";
DROP TABLE "BSCL.Plataforma.BD::COM.PerfilCondicionesOperativas" CASCADE;
DROP TABLE "BSCL.Plataforma.BD::COM.DetallePerfilCondicionesOperativas" CASCADE;
DROP TABLE "BSCL.Plataforma.BD::COM.DetalleCondicionOperativa" CASCADE;
DROP TABLE "BSCL.Plataforma.BD::COM.TipoCondicionOperativa" CASCADE;

DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilCondicionesOperativas"; 

--select * from sys.objects where object_name like '%CondicionOperativa.hdbsequence%';
--select * from sys.objects where object_name like '%Secuencias::Portafolio%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias::DetallePerfilComercial%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias.DetallePerfilProductos%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias.DetalleGrupoActivos%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias.DetallePerfilComercial%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias.DetallePerfilCobros%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias.Ciudad.hdbsequence%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias.EtapaOperacion.hdbsequence::EtapaOperacion%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias.Region.hdbsequence::Region%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias.TipoEtapa.hdbsequence%';
--select * from sys.objects where object_name like '%BSCL.Plataforma.BD.Secuencias.OperacionUsuario.hdbsequence%';

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.CondicionOperativa.hdbsequence::CondicionOperativa";
DROP SEQUENCE "BSCL.Cliente.BD.Secuencias::Portafolio";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::DetallePerfilComercial";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.DetallePerfilProductos.hdbsequence::DetallePerfilProductos";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.DetalleGrupoActivos.hdbsequence::DetalleGrupoActivos";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.DetallePerfilComercial.hdbsequence::DetallePerfilComercial";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.DetallePerfilCobros.hdbsequence::DetallePerfilCobros";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.Ciudad.hdbsequence::Ciudad";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.EtapaOperacion.hdbsequence::EtapaOperacion";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.Region.hdbsequence::Region";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.TipoEtapa.hdbsequence::TipoEtapa";
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias.OperacionUsuario.hdbsequence::OperacionUsuario";
--------------------------------------------------
--      FIN ELIMINAR DEPRECADOS COM
--------------------------------------------------


