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

SET SCHEMA BSCL;
--
--
-- LIBERACION SEMANA: 18-03-2016
--


--################################################
--       MMACHMAR - Inscripcion Accesos LibroOrdenes
--################################################

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

--------------------------------------------------
--      FIN <Inscripcion Accesos>
--------------------------------------------------

--################################################
--    JG - Preferencias Pago Portafolio
--################################################

-- Marca: RecibePagaPortafolio,  'R': recibe, 'P': paga

SET SCHEMA BSCL;

ALTER TABLE "BSCL.Plataforma.BD::INS.TipoOperacion"
ADD("RecibePagaPortafolio" NVARCHAR(1) default '' not null );

UPDATE "BSCL.Plataforma.BD::INS.TipoOperacion" SET "RecibePagaPortafolio" = 'P'
WHERE "Accion" = 'COMPRA' and "Comprobante" = 'S';

UPDATE "BSCL.Plataforma.BD::INS.TipoOperacion" SET "RecibePagaPortafolio" = 'R'
WHERE "Accion" = 'VENTA' and "Comprobante" = 'S';

create column table "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
	"IdPreferenciaPago"			NVARCHAR(40)	not null, 
	"IdTipoOperacion" 			NVARCHAR(6) 	not null default '',
	"IdMedioDePago"				NVARCHAR(3)		not null default '',
	"IdMoneda" 					NVARCHAR(3)		not null default '',
	"UsaPersonaRelacionada"		NVARCHAR(1)		not null default 'N',
	"UsaCuentaBancaria"			NVARCHAR(1)		not null default 'N',
   	"FechaCreacion" 			SECONDDATE  	not null default '0001-01-01 00:00:00',
	"FechaModificacion" 		SECONDDATE 		not null default '0001-01-01 00:00:00',
	"IdUsuarioCreador"  		BIGINT  		not null default 0,
	"IdEstado"					NVARCHAR(3)   	not null default 'ACT', -- 'ACT', 'INA'
	"IdEmpresa"					BIGINT         	not null default 0,
	primary key ("IdPreferenciaPago")
);

-- Ej. INSERT PARA PREFERENCIAS DE PAGO DEFINIDAS POR CORREDORA.
INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
	"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
VALUES('COMACN_CHE_CLP','COMACN','CHE','CLP','N','N',1);

INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
	"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
VALUES('COMACN_EFE_CLP','COMACN','EFE','CLP','N','N',1);

INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
	"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
VALUES('VENACN_CHE_CLP','VENACN','CHE','CLP','N','N',1);

INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
	"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
VALUES('VENACN_EFE_CLP','VENACN','EFE','CLP','N','N',1);

INSERT INTO "BSCL.Plataforma.BD::PAG.PreferenciaPago" (
	"IdPreferenciaPago","IdTipoOperacion","IdMedioDePago","IdMoneda","UsaPersonaRelacionada","UsaCuentaBancaria","IdEmpresa")
VALUES('VENACN_TRA_CLP','VENACN','TRA','CLP','N','S',1);
	
	
drop table "BSCL.Plataforma.BD::COM.DetallePerfilFormasPago" 			cascade;
drop table "BSCL.Plataforma.BD::COM.PerfilFormasPago" 					cascade;	
--------------------------------------------------
--      FIN Preferencias Pago Portafolio
--------------------------------------------------

--################################################
--    NR - Servicios de nominas y transferencias
--################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(225,'BSCL.Tesoreria.BD.Servicios::RecepcionarNominasTransferencias',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(226,'BSCL.Tesoreria.BD.Servicios::GirarComprobantesEgreso',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(230,'BSCL.Tesoreria.BD.Servicios::AnularNominasTransferencias',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(231,'BSCL.Tesoreria.BD.Servicios::ReprocesarNominasTransferencias',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (229, 'BSCL.Custodia.BD.Servicios::EVC.ModificarEventoCapitalCliente' , 1,  'P', 'Modifica un Evento Capital Cliente')

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (237, 'BSCL.Custodia.BD.Servicios::EVC.EliminarEventoCapitalCliente' , 1,  'P', 'Elimina un Evento Capital Cliente')

--------------------------------------------------
--      FIN Servicios de nominas y transferencias
--------------------------------------------------




--################################################
--    JG - Modelo Facturacion
--################################################

drop table "BSCL.Plataforma.BD::COM.DetallePreferenciaFacturacion" 		cascade;
drop table "BSCL.Plataforma.BD::COM.PreferenciaFacturacion" 			cascade;
drop table "BSCL.Plataforma.BD::COM.TipoPreferenciaFacturacion" 		cascade;
drop table "BSCL.Plataforma.BD::COM.DetallePerfilFacturacion" 			cascade;
drop table "BSCL.Plataforma.BD::COM.PerfilFacturacion" 					cascade;


create column table "BSCL.Plataforma.BD::COM.ModeloFacturacion" (
	"IdModeloFacturacion"						NVARCHAR(40)	not null,
	"DescripcionModeloFacturacion"				NVARCHAR(100)   not null,
	"SeparaFacturaPorTransaccion"				NVARCHAR(1)		not null default '', 
	"SeparaFacturaPorInstrumento"				NVARCHAR(1)		not null default '', 
	"FusionaFacturaPorPrecio"					NVARCHAR(1)		not null default '', 
	"IdModoFacturacion"							NVARCHAR(40)	not null default 'NORMAL', -- NORMAL, CONVENIO, CONTRATO
	"IdEntidadModoFacturacion"					BIGINT			not null default 0, 
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"FechaModificacion"							DATE			not null default '0001-01-01 00:00:00',
	"IdUsuarioCreador"							BIGINT			not null default 0,
	"IdEstado"					   				NVARCHAR(3)		not null default 'ACT',
	"PorDefecto"								NVARCHAR(1)		not null default '',
	"IdEmpresa"									BIGINT			not null default 0,
	primary key ("IdModeloFacturacion")
);
 
ALTER TABLE "BSCL.Plataforma.BD::COM.PerfilComercialBase"
DROP("IdPerfilFacturacion");

ALTER TABLE "BSCL.Plataforma.BD::COM.PerfilComercialBase"
DROP("IdPerfilFormasPagoPermitidos");

ALTER TABLE "BSCL.Plataforma.BD::COM.PerfilComercialBase"
ADD("IdModeloFacturacion" NVARCHAR(40) not null default '0');


DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilFacturacion"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilFormasPago";

--------------------------------------------------
--      FIN Modelo Facturacion
--------------------------------------------------


--################################################
--    JG - Division Pais
--################################################

SET SCHEMA "BSCL";
UPDATE "BSCL.Plataforma.BD::REG.Pais" SET "UsaDivisionPais" = 'S' Where "IdPais" = 44;

--------------------------------------------------
--      FIN Division Pais
--------------------------------------------------

--################################################
--    CSI - VALORIZADOR
--################################################
SET SCHEMA "BSCL";

INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10012,'TIME_OUT');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10013,'VALORIZADOR');

CREATE TYPE BSCL."BSCL.Plataforma.BD.Tipos::INS.TareaValorizacion" AS TABLE ("IdTarea" BIGINT,"IdTipoInstrumento" NVARCHAR(3),"IdInstrumento" BIGINT,"FolioCartera" BIGINT,"Nemotecnico" NVARCHAR(40),"Nominales" DECIMAL(26,8),"IdMoneda" NVARCHAR(10),"TipoValorizacion" NVARCHAR(1),"OpcionValorizacion" NVARCHAR(100),"FechaHoy" DATE,"FechaManana" DATE,"TasaCompra" DECIMAL(26,8),"PrecioACompraHoy" DECIMAL(26,8),"MontoValMonLocalACompraHoy" DECIMAL(26,8),"MontoValMonInsACompraHoy" DECIMAL(26,8),"DurationACompraHoy" DECIMAL(26,8),"ConvexidadACompraHoy" DECIMAL(26,8),"PrecioACompraMan" DECIMAL(26,8),"MontoValMonLocalACompraMan" DECIMAL(26,8),"MontoValMonInsACompraMan" DECIMAL(26,8),"DurationACompraMan" DECIMAL(26,8),"ConvexidadACompraMan" DECIMAL(26,8),"TasaMercado" DECIMAL(26,8),"PrecioAMercadoHoy" DECIMAL(26,8),"MontoValMonLocalAMdoHoy" DECIMAL(26,8),"MontoValMonInsAMdoHoy" DECIMAL(26,8),"DurationAMercadoHoy" DECIMAL(26,8),"ConvexidadAMercadoHoy" DECIMAL(26,8),"PrecioAMercadoMan" DECIMAL(26,8),"MontoValMonLocalAMdoMan" DECIMAL(26,8),"MontoValMonInsAMdoMan" DECIMAL(26,8),"DurationAMercadoMan" DECIMAL(26,8),"ConvexidadAMercadoMan" DECIMAL(26,8),"TasaInteresFiscal" DECIMAL(26,8),"PrecioATIFHoy" DECIMAL(26,8),"MontoValMonLocalATIFHoy" DECIMAL(26,8),"MontoValMonInsATIFHoy" DECIMAL(26,8),"DurationATIFHoy" DECIMAL(26,8),"ConvexidadATIFHoy" DECIMAL(26,8),"PrecioATIFMan" DECIMAL(26,8),"MontoValMonLocalATIFMan" DECIMAL(26,8),"MontoValMonInsATIFMan" DECIMAL(26,8),"DurationATIFMan" DECIMAL(26,8),"ConvexidadATIFMan" DECIMAL(26,8) );

create sequence "BSCL"."BSCL.Plataforma.BD.Secuencias::INS.TareaValorizacion" increment by 1 start with 10000 minvalue 1 maxvalue 4611686018427387903 cycle;

CREATE COLUMN TABLE "BSCL"."BSCL.Plataforma.BD::PLA.Depuracion" (
     "IdEmpresa" BIGINT CS_FIXED NOT NULL ,
	 "Fecha" LONGDATE CS_LONGDATE NOT NULL ,
	 "IdUsuario" BIGINT CS_FIXED NOT NULL ,
	 "Servicio" NVARCHAR(255) NOT NULL ,
	 "Bandera" NVARCHAR(80) NOT NULL ,
	 "Detalle" NVARCHAR(520));
	 
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('VALORIZADOR', 'Valorizador', 1,'Plataforma','cl.bcs.BcsValorizador.Valoriza','JAVA');
	 
--------------------------------------------------
--      FIN VALORIZADOR
--------------------------------------------------

--################################################
--    CESPINOSA - CIERRE MODULOS
--################################################
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::CREModuloRequisitoAperturaCierre" ;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::CREModuloRequisitoAperturaCierre" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
alter table "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre"
	add (
		"IdCREModuloEmpresa" 			BIGINT CS_FIXED NOT NULL
	);

alter table "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre"
add constraint "FK_CREModuloEmpresa_CREModuloRequisitoAperturaCierre" foreign key ("IdCREModuloEmpresa")
references "BSCL.Plataforma.BD::PLA.CREModuloEmpresa" ("IdCREModuloEmpresa") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloEmpresa"
	add (
		"FechaProceso" 			NVARCHAR(10)
	);

alter table "BSCL.Plataforma.BD::PLA.CREModuloEmpresa"
	add (
		"TipoEstado" 			CHAR(1) DEFAULT 'C' /* [A]bierto - [C]errado*/
	);
	
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(228,'BSCL.Plataforma.BD.Servicios::CrearRequisitoAperturaCierreModulo',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(234,'BSCL.Plataforma.BD.Servicios::SolicitudAperturaModulo',0,'P','');

insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('CIEMOD',1,'PLA','AUT_CIE_MOD','Cierre de Módulo',1,'S','' ,'' ,0,0,'VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('APEMOD',1,'PLA','AUT_CIE_MOD','Apertura de Módulo',1,'S','' ,'' ,0,0,'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('APERMO','','PLA','APERTURA DE MODULO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CIERMO','','PLA','CIERRE DE MODULO','VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('AUT','Autorizado');

--------------------------------------------------
--      CIERRE MODULOS
--------------------------------------------------



