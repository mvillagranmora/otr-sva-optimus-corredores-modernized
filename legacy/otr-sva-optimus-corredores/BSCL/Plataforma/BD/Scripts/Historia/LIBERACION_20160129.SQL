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

set schema bscl;

--
--
-- LIBERACION SEMANA: 29-01-2016
--

--------------------CORREXIONES ----------------------------------------------
alter table "BSCL"."BSCL.Plataforma.BD::ACC.RolUsuario" DROP ("IdEstado");


delete from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion" WHERE "IdTipoOperacion" IN ('INGTIT','EGRTIT','TRATIT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('INGTIT','INGTIT','INGRESO DE TITULO','INGRESO','CUST','N','N','N','N','N','INGRESO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EGRTIT','EGRTIT','EGRESO DE TITULO','EGRESO','CUST','N','N','N','N','N','EGRESO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('TRATIT','TRATIT','TRASPASO DE TITULOS','TRASPASO','CUST','N','N','N','N','N','TRASPASO','','N','');

create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");

alter table "BSCL"."BSCL.Orden.BD::ORD.Orden" alter ("IdTipoReajuste" NVARCHAR(3) not null default '');
alter table "BSCL"."BSCL.Orden.BD::ORD.Transaccion" alter ("IdTipoReajuste" NVARCHAR(3) not null default '');

commit;


SELECT DISTINCT "TT"."IdTipoOperacionTipoInstrumento" 
, "TT"."IdTipoOperacion" 
, "TT"."IdTipoInstrumento" 
, "TT"."IdInstrumento" 
, "TT"."Nemotecnico" 
, "TT"."IdEstado" 
, "TIOP"."Nombre" 
, "TIOP"."Codigo" 
, "TIOP"."Accion" 
, "TIOP"."Accion" AS "CodigoAccion"
, "TIOP"."AccionOperacion" 
, "TIOP"."IdTipoProducto" 
, "TIOP"."Comprobante" 
, "TIOP"."EsOrden" 
, "TIOP"."EsVentaCorta" 
, "TIOP"."EsTransaccion" 
FROM "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS tt
  LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion"  as tiop ON tt."IdTipoOperacion" = tiop."IdTipoOperacion"
WHERE "TIOP"."IdTipoOperacion" IN ('INGTIT','EGRTIT','TRATIT');


SELECT *
FROM "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS tt
WHERE "TT"."IdTipoOperacion" IN ('INGTIT','EGRTIT','TRATIT');

  LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion"  as tiop ON tt."IdTipoOperacion" = tiop."IdTipoOperacion"
--------------------SCRIPT ROLES----------------------------------------------
--Secuencia tabla Rol
CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::Rol"
       START     WITH 3
       INCREMENT BY 1
       MINVALUE  1; 
--Secuencia tabla Rol
CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::RolPerfil"
       START     WITH 3
       INCREMENT BY 1
       MINVALUE  1; 
--Agregar IdEstado a la tabla perfiles
alter table "BSCL"."BSCL.Plataforma.BD::ACC.Perfil" ADD ("IdEstado" NVARCHAR(3));
Update "BSCL"."BSCL.Plataforma.BD::ACC.Perfil" set "IdEstado" = 'ACT';

--Agregar a servicios
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(126,'BSCL.Plataforma.BD.Servicios::ACC.CrearRol',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(128,'BSCL.Plataforma.BD.Servicios::ACC.AsignarPerfilRol',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(129,'BSCL.Plataforma.BD.Servicios::ACC.EliminarPerfilRol',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(132,'BSCL.Plataforma.BD.Servicios::ACC.ActualizarRol',0,'P','');;
--Agregar estados Roles
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
	values(6, 'ACT', 'Rol', 'Activo');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
	values(7, 'INA', 'Rol', 'Inactivo');
--------------------FIN SCRIPT ROLES---------------------------------------
--------------------PERFILES-----------------------------------------------
--Secuencia tabla Perfil
CREATE SEQUENCE "BSCL"."BSCL.Plataforma.BD.Secuencias::Perfil"
       START     WITH 3
       INCREMENT BY 1
       MINVALUE  1; 
--Agregar a servicios
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(138,'BSCL.Plataforma.BD.Servicios::ACC.CrearPerfil',0,'P','');

--Agregar estados Perfiles
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
	values(8, 'ACT', 'Perfil', 'Activo');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
	values(9, 'INA', 'Perfil', 'Inactivo');

--Crear Nuevas Tablas accesos
drop table "BSCL.Plataforma.BD::ACC.Acceso" cascade;
create column table "BSCL.Plataforma.BD::ACC.Acceso" (
   "IdAcceso"  				BIGINT              not null, -- Id del acceso
   "NombreAcceso"			NVARCHAR(100)		not null, -- Nombre a desplegar en la pantalla de asignacion de accesos
   "TipoAcceso"				NVARCHAR(5)			not null, -- MENU1,MENU2,MENU3,BOTON,CMBX, INPT,SRV 
   "IdOpcionMenu"       	INT          	    not null, -- Id de las opciones en el menu lateral (3 niveles de profundidad)
   "IdFormulario"	 		INT					not null, -- el id de la pantalla ej: Consulta Usuarios.
   "IdComponente"         	INT				    not null, -- el id del componente de la pantalla ej: boton, combo, input, etc
   "IdServicio"			  	BIGINT			    not null, -- el id del servicio que necesita el componente (entidad servicio)
   primary key ("IdAcceso")
);

drop table "BSCL.Plataforma.BD::ACC.PerfilAcceso" cascade;
create column table "BSCL.Plataforma.BD::ACC.PerfilAcceso" (
   "IdAccesoPerfil"  		BIGINT              not null,
   "IdEmpresa"				BIGINT				not null,
   "IdPerfil"  				BIGINT              not null, 
   "IdAcceso"				BIGINT				not null,
   "IdEstado"		 		NVARCHAR(3)			not null,
   primary key ("IdAccesoPerfil")
);

--Agregar nuevos Accesos
set schema "BSCL";
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso", "TipoAcceso","IdOpcionMenu","IdFormulario", "IdComponente", "IdServicio")  
VALUES(1,'PLATAFORMA', 'MENU1',1,0,0,0);
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso", "TipoAcceso","IdOpcionMenu","IdFormulario", "IdComponente", "IdServicio")
VALUES(2,'Usuarios', 'MENU2',1,1,1,1);
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso", "TipoAcceso","IdOpcionMenu","IdFormulario", "IdComponente", "IdServicio")
VALUES(3,'Crear Usuario', 'BTN',0,1,1,2);
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso", "TipoAcceso","IdOpcionMenu","IdFormulario", "IdComponente", "IdServicio")
VALUES(4,'Modificar Usuario', 'BTN',0,1,1,3);
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso", "TipoAcceso","IdOpcionMenu","IdFormulario", "IdComponente", "IdServicio")
VALUES(5,'Administrar Roles', 'BTN',0,1,1,4);
INSERT INTO "BSCL.Plataforma.BD::ACC.Acceso" ( "IdAcceso","NombreAcceso", "TipoAcceso","IdOpcionMenu","IdFormulario", "IdComponente", "IdServicio")
VALUES(6,'Administrar Perfiles', 'BTN',0,1,1,5);

INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (1,1,1,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (2,1,1,2,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (3,1,1,3,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (4,1,1,4,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (5,1,1,5,'ACT');
INSERT INTO "BSCL.Plataforma.BD::ACC.PerfilAcceso"("IdAccesoPerfil", "IdEmpresa", "IdPerfil", "IdAcceso", "IdEstado")
VALUES (6,1,1,6,'ACT');

--------------------SCRIPT IdTipoReajuste----------------------------------------------
--se agranda tipo de reajuste de nvarchar de 2 a nvarchar de 3
alter table "BSCL.Plataforma.BD::PLA.TipoReajuste" 
       alter ("IdTipoReajuste" NVARCHAR(3) not null default '');

alter table "BSCL.Plataforma.BD::INS.Instrumento"
       alter ("IdTipoReajuste" NVARCHAR(3) not null default '');

alter table "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
       alter ("IdTipoReajuste" NVARCHAR(3) not null default '');

alter table "BSCL.Plataforma.BD::LIM.Limite"
       alter ("IdTipoReajuste" NVARCHAR(3) not null default '');



drop type "BSCL.Plataforma.BD::INS.BolsaInstrumentoPreciosActual"

CREATE TYPE "BSCL.Plataforma.BD::INS.BolsaInstrumentoPreciosActual" AS TABLE  ( "IdInstrumento" BIGINT CS_FIXED,
 "Nemotecnico" NVARCHAR(40)  CS_STRING,
 "ISIN" NVARCHAR(40)  CS_STRING,
 "CorteMinimo" DECIMAL(26,8) CS_FIXED,
 "EstaInscrito" NVARCHAR  CS_STRING,
 "EsDivisible" NVARCHAR  CS_STRING,
 "RazonSocial" NVARCHAR(100)  CS_STRING,
 "Lista" NVARCHAR(520)  CS_STRING,
 "FechaEmision" DAYDATE CS_DAYDATE,
 "FechaVencimiento" DAYDATE CS_DAYDATE,
 "IdInstrumentoRelacionado" BIGINT CS_FIXED,
 "IdEmisor" NVARCHAR(40)  CS_STRING,
 "IdTipoReajuste" NVARCHAR(3)  CS_STRING,
 "Reajustable" NVARCHAR  CS_STRING,
 "IdMoneda" NVARCHAR(3)  CS_STRING,
 "IdTipoInstrumento" NVARCHAR(3)  CS_STRING,
 "IdEstado" NVARCHAR(3)  CS_STRING,
 "DecimalesPrecio" INT CS_INT,
 "NombreTipoInstrumento" NVARCHAR(100)  CS_STRING,
 "IdClasificacionTipoInstrumento" NVARCHAR(3)  CS_STRING,
 "NombreClasificacionTipoInstrumento" NVARCHAR(100)  CS_STRING,
 "RazonSocialEmisor" NVARCHAR(100)  CS_STRING,
 "IdBolsa" NVARCHAR(3)  CS_STRING,
 "IdBolsaInstrumentoPrecios" BIGINT CS_FIXED,
 "UltimoPrecioTransado" DECIMAL(26,8) CS_FIXED,
 "UltimoPrecioCompra" DECIMAL(26,8) CS_FIXED,
 "UltimoPrecioVenta" DECIMAL(26,8) CS_FIXED,
 "FechaCierre" DAYDATE CS_DAYDATE ,
 "Tamano" BIGINT CS_FIXED);


DELETE FROM  "BSCL.Plataforma.BD::PLA.TipoReajuste";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoReajuste" ( "IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado" )  
VALUES('CLP','CLP','No Reajustable','N','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoReajuste" ( "IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado" )  
VALUES('USD','USD','Dolar','S','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoReajuste" ( "IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado" )  
VALUES('EUR','EUR','Euro','S','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoReajuste" ( "IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado" )  
VALUES('UF','UF','Unidad de fomento','S','ACT');
commit;

--Agregar campos a la tabla Log
alter table "BSCL.Plataforma.BD::PLA.Log"
	add (
		"IdMonedaOrigen"			NVARCHAR(3),
	 	"IdMonedaLiquidacion"		NVARCHAR(3),
	 	"TipoCambio"				NVARCHAR(40)
	);
	
alter table "BSCL.Plataforma.BD::PLA.Log"
	alter (
		"IdRegistro"			NVARCHAR(40)
	);
	

--============================================================================
-------------------- JG: INICIO SCRIPT CONDICIONES OPERATIVAS ----------------
--============================================================================


drop table "BSCL.Plataforma.BD::COM.OperacionUsuario"	 					cascade;
drop table "BSCL.Plataforma.BD::COM.TipoEtapa"								cascade;
drop table "BSCL.Plataforma.BD::COM.EtapaOperacion"							cascade;
drop table "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa" 				cascade;
drop table "BSCL.Plataforma.BD::COM.CondicionOperativa" 					cascade;
drop table "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" 			cascade;
drop table "BSCL.Plataforma.BD::COM.TipoCondicion" 							cascade;
--
drop table "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" 		cascade;
drop table "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas"				cascade;
drop table "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas"	cascade;
drop table "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas" 		cascade;


/* Maestros */
create column table "BSCL.Plataforma.BD::COM.OperacionUsuario" (
	"IdOperacionUsuario"						NVARCHAR(10)	not null,
	"Nombre"									NVARCHAR(100)	not null default '',
	"IdTipoOperacion"							NVARCHAR(6)     not null default '', -- desde INS->TipoOperacion
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	--"IdEmpresa"         						BIGINT          not null default 0,  
	primary key ("IdOperacionUsuario")
);

create column table "BSCL.Plataforma.BD::COM.TipoEtapa" (
	"IdTipoEtapa"								NVARCHAR(20)	not null,
	"Nombre"									NVARCHAR(100)	not null default '', 
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	primary key ("IdTipoEtapa")
);

create column table "BSCL.Plataforma.BD::COM.EtapaOperacion" (
	"IdEtapaOperacion"							NVARCHAR(40)   	not null, 
	"IdOperacionUsuario"						NVARCHAR(10)	not null default '',
	"IdTipoEtapa"								NVARCHAR(20)    not null default '', 
	"NombreCorto"								NVARCHAR(40)	not null default '', -- para mostrar en check
	"Nombre"									NVARCHAR(100)	not null default '',
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	primary key ("IdEtapaOperacion")
);

create column table "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" (
	"IdTipoComportamientoCondicion"				NVARCHAR(40)	not null,
	"Nombre"									NVARCHAR(100)	not null default '',
	primary key ("IdTipoComportamientoCondicion")
);

create column table "BSCL.Plataforma.BD::COM.TipoCondicion" (
	"IdTipoCondicion"							NVARCHAR(40)	not null,
	"Nombre"									NVARCHAR(100)	not null default '',
	"RequiereAtributoX"							NVARCHAR(1)		not null default 'N',
	"RequiereAtributoY"							NVARCHAR(1)		not null default 'N',	
	"ComportamientoPorDefecto"					NVARCHAR(40)	not null default '', -- ADVERTENCIA, RECHAZO, AUTORIZACION
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	primary key ("IdTipoCondicion")
);

create column table "BSCL.Plataforma.BD::COM.CondicionOperativa" (
	"IdCondicionOperativa"						NVARCHAR(80)	not null,
	"IdTipoCondicion"							NVARCHAR(40)	not null default '',
	--"CodigoTipoCondicion"						NVARCHAR(40)	not null default '',
	"IdOperacionUsuario"						NVARCHAR(10)   	not null default '',
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"IdEmpresa"         						BIGINT          not null default 0,
	primary key ("IdCondicionOperativa")
);

create column table "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa" (
	"IdEtapaCondicionOperativa"					BIGINT       	not null, 
	"IdEtapaOperacion"							NVARCHAR(40)  	not null default '',
	"IdCondicionOperativa"						NVARCHAR(80)	not null default '', 
	"IdEstado"					   				NVARCHAR(3)		not null default 'ACT',
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	primary key ("IdEtapaCondicionOperativa")
);

-- Plantilla Condiciones Operativas
create column table "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas" (
	"IdPlantillaCondicionesOperativas"			NVARCHAR(40)	not null,
	"Descripcion"								NVARCHAR(100)	not null default '',
	"IdCategoriaInversionista"					NVARCHAR(40)	not null default '0',
	"EsPerfilPersonalizado"						NVARCHAR(1)		not null default 'N',
	"TipoEntidadReferencia"						NVARCHAR(40)	not null default '',
	"IdReferencia"								BIGINT			not null default 0,
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"FechaModificacion"							DATE			not null default '0001-01-01 00:00:00',
	"IdEstado"					   				NVARCHAR(3)		not null default 'ACT',
	"PorDefecto"								NVARCHAR(1)		not null default '',
	"IdEmpresa"									BIGINT			not null default 0,
	primary key ("IdPlantillaCondicionesOperativas")
);

create column table "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas" (
	"IdDetallePlantillaCondicionesOperativas"	BIGINT          not null, 
	"IdPlantillaCondicionesOperativas"			NVARCHAR(40)	not null default '',
	"IdCanalIngreso"							VARCHAR(3)      not null default '',
	"IdMoneda"									NVARCHAR(3)    	not null default '',
	"IdOperacionUsuario"						NVARCHAR(10)   	not null default '', 
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"IdEstado"					   				NVARCHAR(3)   	not null default 'ACT',
	primary key ("IdDetallePlantillaCondicionesOperativas")
);

create column table "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas" (
	"IdGrupoCondicionesOperativas"				BIGINT          not null, 
	"IdDetallePlantillaCondicionesOperativas"	BIGINT          not null default 0,
	"IdCondicionOperativa"						NVARCHAR(80)	not null default '',					
	"AtributoX"									NVARCHAR(500)	not null default '',
	"AtributoY"									NVARCHAR(500)	not null default '',	
	"IdComportamiento"							NVARCHAR(40)	not null default '', -- ADVERTENCIA, RECHAZO, AUTORIZACION
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"IdEstado"					   				NVARCHAR(3)   	not null default 'ACT',
	primary key ("IdGrupoCondicionesOperativas")
);

create column table "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas" (
	"IdEtapaGrupoCondicionesOperativas"			BIGINT          not null, 
	"IdGrupoCondicionesOperativas"				BIGINT			not null default 0,
	"IdEtapaCondicionOperativa"					BIGINT			not null default 0,
	"FechaCreacion"								DATE			not null default '0001-01-01 00:00:00',
	"IdEstado"					   				NVARCHAR(3)   	not null default 'ACT',
	primary key ("IdEtapaGrupoCondicionesOperativas")
);


/*==============================================================*/
/* SECUENCIAS:                                                  */
/*==============================================================*/

drop sequence "BSCL.Plataforma.BD.Secuencias::CondicionOperativa";
create sequence "BSCL.Plataforma.BD.Secuencias::CondicionOperativa" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaGrupoCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaGrupoCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::EtapaOperacion";
create sequence "BSCL.Plataforma.BD.Secuencias::EtapaOperacion" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::GrupoCondicionesOperativas";
create sequence "BSCL.Plataforma.BD.Secuencias::GrupoCondicionesOperativas" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::OperacionUsuario";
create sequence "BSCL.Plataforma.BD.Secuencias::OperacionUsuario" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::TipoEtapa";
create sequence "BSCL.Plataforma.BD.Secuencias::TipoEtapa" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

----------------------- DATOS ----------------------

-- Condiciones Operativas
DELETE FROM "BSCL.Plataforma.BD::COM.OperacionUsuario";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoEtapa";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaOperacion";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.CondicionOperativa";
DELETE FROM "BSCL.Plataforma.BD::COM.EtapaGrupoCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.GrupoCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePlantillaCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.PlantillaCondicionesOperativas";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion";
DELETE FROM "BSCL.Plataforma.BD::COM.TipoCondicion" ;
--select * from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion"

--OperacionUsuario
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('ENROCLI','Enrolamiento Cliente','',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('ENROPER','Enrolamiento Persona','',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('NUEPORTF','Creación Portafolio','',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('COMACN','Compra Acciones RV','COMACN',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.OperacionUsuario" ("IdOperacionUsuario","Nombre","IdTipoOperacion","FechaCreacion")
VALUES ('VENACN','Venta Acciones RV','VENACN',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.OperacionUsuario"

-- Tipo Etapa
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('INGRESO','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('ASINACION','Asignación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('ENVFACT','Envío a Facturar',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('GCOMFACT','Generación comprobante Facturación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoEtapa" ("IdTipoEtapa","Nombre","FechaCreacion")
VALUES ('FACTDTE','Facturación DTE',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.TipoEtapa"

-- Etapa Operacion
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_ENROCLI','ENROCLI','INGRESO','Ingreso de cliente','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_ENROPER','ENROPER','INGRESO','Ingreso de cliente','Ingreso',NOW());
--
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_NUEPORTF','NUEPORTF','INGRESO','Ingreso de portafolio','Ingreso',NOW());
--
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_COMACN','COMACN','INGRESO','Ingreso de compra de Acciones RV','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('ASINACION_COMACN','COMACN','ASINACION','Asignación de compra de Acciones RV','Asignación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('ENVFACT_COMACN','COMACN','ENVFACT','Envío a Facturar Compra de Acciones RV','Envío a Facturar',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('GCOMFACT_COMACN','COMACN','GCOMFACT','Generación comprobante Facturación Compra de Acciones RV','Generación comprobante Facturación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('FACTDTE_COMACN','COMACN','FACTDTE','Facturación DTE Compra de Acciones RV','Facturación DTE',NOW());
--
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('INGRESO_VENACN','VENACN','INGRESO','Ingreso de venta de Acciones RV','Ingreso',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('ASINACION_VENACN','VENACN','ASINACION','Asignación de venta de Acciones RV','Asignación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('ENVFACT_VENACN','VENACN','ENVFACT','Envío a Facturar venta de Acciones RV','Envío a Facturar',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('GCOMFACT_VENACN','VENACN','GCOMFACT','Generación comprobante Facturación venta de Acciones RV','Generación comprobante Facturación',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaOperacion" ("IdEtapaOperacion","IdOperacionUsuario","IdTipoEtapa","Nombre","NombreCorto","FechaCreacion")
VALUES ('FACTDTE_VENACN','VENACN','FACTDTE','Facturación DTE venta de Acciones RV','Facturación DTE',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.EtapaOperacion"

-- TipoComportamientoCondicion
INSERT INTO "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" ("IdTipoComportamientoCondicion","Nombre")
VALUES ('ADVERTENCIA','Advertencia');
INSERT INTO "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" ("IdTipoComportamientoCondicion","Nombre")
VALUES ('RECHAZO','Rechazo');
INSERT INTO "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion" ("IdTipoComportamientoCondicion","Nombre")
VALUES ('AUTORIZACION','Requiere Autorización');
-- SELECT * FROM "BSCL.Plataforma.BD::COM.TipoComportamientoCondicion"

-- TipoCondicion
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('VIGDOCID','Vigencia Documento de Identidad','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('PEP','Persona Políticame Expuesta (PEP)','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('COMPLIANCE','Cliente en Lista Compliance','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('DOF','Requiere declaración origen de Fondos (DOF)','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('SALDONEG','Operar Saldo Negativo','N','N','AUTORIZACION',NOW());
INSERT INTO "BSCL.Plataforma.BD::COM.TipoCondicion" ("IdTipoCondicion","Nombre","RequiereAtributoX","RequiereAtributoY","ComportamientoPorDefecto","FechaCreacion")
VALUES ('NOMINAL','Opera Nominal','N','N','AUTORIZACION',NOW());
-- SELECT * FROM "BSCL.Plataforma.BD::COM.TipoCondicion"

-- Condicion Operativa
-- Para: ENROCLI
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROCLI_VIGDOCID','VIGDOCID','ENROCLI',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROCLI_PEP','PEP','ENROCLI',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROCLI_COMPLIANCE','COMPLIANCE','ENROCLI',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROCLI_DOF','DOF','ENROCLI',NOW(),1);

-- Para: ENROPER
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROPER_VIGDOCID','VIGDOCID','ENROPER',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROPER_PEP','PEP','ENROPER',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('ENROPER_DOF','DOF','ENROPER',NOW(),1);

-- Para: NUEPORTF
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('NUEPORTF_VIGDOCID','VIGDOCID','NUEPORTF',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('NUEPORTF_COMPLIANCE','COMPLIANCE','NUEPORTF',NOW(),1);

-- Para COMACN
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('COMACN_COMPLIANCE','COMPLIANCE','COMACN',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('COMACN_SALDONEG','SALDONEG','COMACN',NOW(),1);

-- Para VENACN
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('VENACN_COMPLIANCE','COMPLIANCE','VENACN',NOW(),1);
INSERT INTO "BSCL.Plataforma.BD::COM.CondicionOperativa" ("IdCondicionOperativa","IdTipoCondicion","IdOperacionUsuario","FechaCreacion","IdEmpresa")
VALUES ('VENACN_NOMINAL','NOMINAL','VENACN',NOW(),1);

-- Etapa Condicion Operativa - POR DEFAULT TODAS
INSERT INTO "BSCL.Plataforma.BD::COM.EtapaCondicionOperativa" ("IdEtapaCondicionOperativa","IdEtapaOperacion","IdCondicionOperativa","IdEstado","FechaCreacion")
SELECT "BSCL"."BSCL.Plataforma.BD.Secuencias::EtapaCondicionOperativa".NEXTVAL,"IdEtapaOperacion","IdCondicionOperativa",'ACT',NOW() FROM "BSCL.Plataforma.BD::COM.CondicionOperativa" CO, "BSCL.Plataforma.BD::COM.EtapaOperacion" EO
WHERE CO."IdOperacionUsuario" = EO."IdOperacionUsuario" order by "IdEtapaOperacion","IdCondicionOperativa";

-- Inscripcion del servicio de Creacion de Plantillas
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(136,'BSCL.Plataforma.BD.Servicios::COM.CrearPlantillaCondicionesOperativas',0,'P','');

--============================================================================
---------------------- JG: FIN SCRIPT CONDICIONES OPERATIVAS -----------------
--============================================================================


---------Servicios Mantenedor Caja---------------
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(127,'BSCL.Tesoreria.BD.Servicios::CrearCaja',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(131,'BSCL.Tesoreria.BD.Servicios::CrearCajaMoneda',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(135,'BSCL.Tesoreria.BD.Servicios::CrearCajaUsuario',0,'P','');

--------- Fin Servicios Mantenedor Caja-------------






--==============================================================================
-----------------------------SCRIPT AGENDA-PROGRAMAS-TIPO PROGRAMAS-------------

drop table "BSCL.Plataforma.BD::BAT.TipoPrograma" cascade;
drop table "BSCL.Plataforma.BD::BAT.Agenda" cascade;
drop table "BSCL.Plataforma.BD::BAT.ParametrosAgenda" cascade;
drop table "BSCL.Plataforma.BD::BAT.Programa" cascade;
drop table "BSCL.Plataforma.BD::BAT.ParametrosPrograma" cascade;

drop sequence "BSCL.Plataforma.BD.Secuencias::ParametrosAgenda" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::Programa" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::Agenda" cascade;
drop sequence "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma" cascade;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

/* ************************************

   TABLAS MAESTRAS

************************************ */

/*
ASIG_CRONO |  tarea para la asignacion cronologica  de ordenes |  ??   | ORDENES

*/
-- Define los tipos de tareas conocidos por el administrador de tarea
 CREATE ROW  TABLE "BSCL.Plataforma.BD::BAT.TipoPrograma" (
	 "IdTipoPrograma" 			NVARCHAR(40) 			NOT NULL ,
	 "Descripcion" 			NVARCHAR(80)		 	NOT NULL DEFAULT '', 
	 "MaximoIntentos" 		INTEGER 				DEFAULT 0 ,
	 "Modulo" 				NVARCHAR(40)			NOT NULL,
	 "NombreClasePrograma"  NVARCHAR(200) 		NOT NULL,
	 "TipoClasePrograma"	NVARCHAR(40)		NOT NULL,	 
	 PRIMARY KEY ("IdTipoPrograma")
 );
	
 
/* ************************************

   TABLAS TRABAJO

************************************ */
--
CREATE ROW TABLE "BSCL.Plataforma.BD::BAT.Agenda" (
	 "IdAgenda" 			BIGINT				NOT NULL ,
	 "IdTipoPrograma" 	    	NVARCHAR(40)		NOT NULL ,
	 "EsDemonio"   			NVARCHAR(1)			NOT NULL DEFAULT 'N',  --S/N 
	 "DiaDeMes" 			NVARCHAR(2) 		NOT NULL ,  --01 AL 31 o ** 
	 "DiaDeSemana" 			NVARCHAR(1) 		NOT NULL ,  --1  AL 7  o *  1=Lunes
	 "Mes" 					NVARCHAR(2) 		NOT NULL ,  --01 AL 12 o **
	 "Hora" 				NVARCHAR(2) 		NOT NULL ,  --00 AL 23 o **
	 "Minuto" 				NVARCHAR(2) 		NOT NULL ,  --00 AL 59 o **
	 "SoloDiasHabiles" 		NVARCHAR(1) 		NOT NULL DEFAULT 'N',  --S/N
	 "SoloPrincipioMes" 	NVARCHAR(1) 		NOT NULL DEFAULT 'N',  --S/N
	 "SoloFinDeMes" 		NVARCHAR(1) 		NOT NULL DEFAULT 'N',  --S/N
	 "habilitado" 			NVARCHAR(1) 		NOT NULL DEFAULT 'S', --S/N
	 PRIMARY KEY ("IdAgenda")
 );





 CREATE ROW TABLE "BSCL.Plataforma.BD::BAT.ParametrosAgenda" (
	 "IdAgenda" 			BIGINT			NOT NULL ,
	 "Orden" 				INTEGER			NOT NULL , --
	 "NombreParametro" 		NVARCHAR(40) 	NOT NULL , --
	 "TipoDato" 			NVARCHAR(20)	NOT NULL , -- STRING, INT, DATE(yyyy-mm-dd), SECONDATE(yyyy-mm-dd hh24:hh:ss), DECIMAL
	 "Valor" 				NVARCHAR(40) 	NOT NULL  --
	 
 );


CREATE ROW TABLE "BSCL.Plataforma.BD::BAT.Programa" (
	 "IdPrograma" 			BIGINT			NOT NULL ,
	 "IdGrupoParametro" 	BIGINT			NULL, --ID de grupo de parametros utilizados para el programa
	 "IdAgenda" 			BIGINT			, --Existen Programas on demand que no tienen Agenda
	 "IdTipoPrograma" 			NVARCHAR(40) 	NOT NULL ,	  
	 "Intento" 				INTEGER 		DEFAULT 0 ,--Numero del intento
	 "IdUsuario" 			NVARCHAR(40) 	NOT NULL , --Id Usuario o Sistema que inicia el programa	 
	 "idEstado" 			NVARCHAR(3) 	NOT NULL , --PEN PENDIENTE / ASI ASIGNADA EJECUCION / TER TERMINADO / ERR ERROR
	 "Resultado" 			NVARCHAR(80) 	NOT NULL DEFAULT '', --Mensaje de salida	
	 "fechaCreacion" 		SECONDDATE 		NOT NULL , --Fecha Creacion
	 "fechaEjecucion" 		SECONDDATE 		DEFAULT '0001-01-01 00:00:00', --Fecha Inicio Ejecucion
	 "fechaFin" 			SECONDDATE		DEFAULT '0001-01-01 00:00:00', --Fecha Termino o Error	  
	 PRIMARY KEY ("IdPrograma")
 );


 CREATE COLUMN TABLE "BSCL.Plataforma.BD::BAT.ParametrosPrograma" (
 	 "IdGrupoParametro" 	BIGINT			NOT NULL ,
	 "IdPrograma" 			BIGINT			NOT NULL ,
	 "Orden" 				INTEGER			NOT NULL , --
	 "NombreParametro" 		NVARCHAR(40) 	NOT NULL ,
	 "Valor" 				NVARCHAR(40) 	NOT NULL , --
	 PRIMARY KEY ("IdGrupoParametro")
 );
 
 
 


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */


  
 
 
alter table "BSCL.Plataforma.BD::BAT.Programa"
add constraint "FK_Programa_TipoPrograma" foreign key ("IdTipoPrograma")
references "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma") on delete restrict on update restrict;
    
alter table "BSCL.Plataforma.BD::BAT.ParametrosPrograma"
add constraint "FK_ParametrosPrograma_Programa" foreign key ("IdPrograma")
references "BSCL.Plataforma.BD::BAT.Programa" ("IdPrograma") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::BAT.Agenda"
add constraint "FK_Agenda_TipoTarea" foreign key ("IdTipoPrograma")
references "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma") on delete restrict on update restrict;


  
/*==============================================================*/
/* secuence:                                                  */
/*==============================================================*/

/* Table: */

create sequence "BSCL.Plataforma.BD.Secuencias::Agenda" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::ParametrosAgenda" increment by 1 start with 50000 minvalue 50000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::Programa" increment by 1 start with 60000 minvalue 60000 maxvalue 4611686018427387903 no cycle;
create sequence "BSCL.Plataforma.BD.Secuencias::ParametrosPrograma" increment by 1 start with 70000 minvalue 70000 maxvalue 4611686018427387903 no cycle;


delete from "BSCL.Plataforma.BD::BAT.ParametrosPrograma" cascade;
delete from "BSCL.Plataforma.BD::BAT.ParametrosAgenda" cascade;
delete from "BSCL.Plataforma.BD::BAT.Programa" cascade;
delete from "BSCL.Plataforma.BD::BAT.Agenda" cascade;
delete from "BSCL.Plataforma.BD::BAT.TipoPrograma" cascade;


  
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ASIG_CRONO_AUTOMATIC', 'Asignación crónologica automatica', 3,'Ordenes','cl.bcs.scheduler.orders.BcsJobOrderChronologiAllocationReader','JAVA');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ASIG_CRONO', 'Asignación crónologica', 3,'Ordenes','cl.bcs.scheduler.orders.BcsJobOrderChronologicAllocation','JAVA');
INSERT INTO "BSCL.Plataforma.BD::BAT.Agenda" VALUES(1, 'ASIG_CRONO_AUTOMATIC', 'N','**','*','**','**','50','N','N','N','S');
INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" VALUES(1,1,0,'ASIG_CRONO',0,'System','PEN','Esperando ejecución','2015-12-14 11:43:00',v_fechaEjecucion,'0001-01-01 00:00:00');
INSERT INTO "BSCL.Plataforma.BD::BAT.ParametrosAgenda" VALUES (1,0,'IdLibroOrdenes','INT','1');
INSERT INTO "BSCL.Plataforma.BD::BAT.ParametrosAgenda" VALUES (2,0,'IdLibroOrdenes','INT','1');
INSERT INTO "BSCL.Plataforma.BD::BAT.ParametrosAgenda" VALUES (1,0,'IdLibroOrdenes','INT','2');
INSERT INTO "BSCL.Plataforma.BD::BAT.ParametrosPrograma" VALUES (1,1,0,'IdLibroOrdenes','1');


--------- FIN DE SCRIPT -------------

drop table "BSCL.Orden.BD::ORD.AsignacionPaso" cascade;
drop table "BSCL.Orden.BD::ORD.OrdenPaso" cascade;
drop table "BSCL.Orden.BD::ORD.TransaccionPaso" cascade;
drop table "BSCL.Orden.BD::ORD.AsignacionResultado" cascade;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::AsignacionPaso"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::OrdenPaso"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::TransaccionPaso"      CASCADE;
DROP SEQUENCE "BSCL.Orden.BD.Secuencias::AsignacionResultado"      CASCADE;
   
  
 
 
  CREATE ROW TABLE "BSCL.Orden.BD::ORD.OrdenPaso" (
  	 "IdPrograma"								BIGINT			NOT NULL ,
 	 "IdOrdenPaso" 								BIGINT			NOT NULL ,
	 "IdClasificacionTipoInstrumento" 			NVARCHAR(3)		NOT NULL ,
	 "IdTipoInstrumento" 						NVARCHAR(6)		NOT NULL , 
	 "IdBolsa" 									NVARCHAR(3) 	NOT NULL ,
	 "IdTipoOperacion" 							NVARCHAR(6) 	NOT NULL , 
 	 "idCondicionDeLiquidacion" 				NVARCHAR(2)		NOT NULL ,
	 "Precio" 									DECIMAL(26,8)	NOT NULL ,
	 "CantidadDisponible"						DECIMAL(26,8)	NOT NULL ,
	 "CantidadAsignada"							DECIMAL(26,8)	NOT NULL ,
 	 "Monto" 									DECIMAL(26,8) 	NOT NULL ,
	 "IdTipoCanalDistribucion" 					NVARCHAR(3) 	NOT NULL ,
 	 "FechaIngreso"							 	TIMESTAMP		NOT NULL ,
	 "FechaOperacion" 							SECONDDATE		NOT NULL ,
	 "IdLibroOrdenes" 							INTEGER			NOT NULL ,
	 "IdTipoPrecio" 							NVARCHAR(1)				,
	 "IdTipoCanalIngreso"						NVARCHAR(3)		 
 );
 
  CREATE ROW TABLE "BSCL.Orden.BD::ORD.AsignacionPaso" (
  	 
 	 "IdAsignacionPaso" 		BIGINT			NOT NULL ,
	 "IdEstadoFacturacion" 		NVARCHAR(3)		NOT NULL ,
	 "CantidadAsignada" 		BIGINT			NOT NULL , --
	 "FechaAsignacion" 			SECONDDATE	 	NOT NULL ,
	 "IdTransaccionPaso" 		BIGINT 			NOT NULL , 
	 "IdOrdenPaso" 				BIGINT			NOT NULL

	 
	 
 );
 
 CREATE ROW TABLE "BSCL.Orden.BD::ORD.AsignacionResultado" (
 	 "IdAsignacionResultado"	BIGINT			NOT NULL ,
	 "IdEstadoFacturacion" 		NVARCHAR(3)		NOT NULL ,
	 "CantidadAsignada" 		BIGINT			NOT NULL , --
	 "FechaAsignacion" 			SECONDDATE	 	NOT NULL ,
	 "IdTransaccionPaso" 		BIGINT 			NOT NULL , 
	 "IdOrdenPaso" 				BIGINT			NOT NULL 
	 
 );
 
CREATE ROW TABLE "BSCL.Orden.BD::ORD.TransaccionPaso" (
	 "IdPrograma" 								BIGINT			not null ,
 	 "IdTransaccionPaso"						BIGINT			NOT NULL ,
	 "IdClasificacionTipoInstrumento" 			NVARCHAR(3)		NOT NULL ,
	 "IdTipoInstrumento" 						NVARCHAR(6)		NOT NULL , --
	 "IdBolsa" 									NVARCHAR(3) 	,
	 "IdTipoOperacion" 							NVARCHAR(6) 	, --
 	 "idCondicionDeLiquidacion" 				NVARCHAR(2)		,
	 "Precio" 									DECIMAL(26,8)	,
	 "CantidadTotal"							DECIMAL(26,8)	,
	 "CantidadDisponible"						DECIMAL(26,8)	,
	 "CantidadAsignada"							DECIMAL(26,8)	NOT NULL ,
	 "Monto" 									DECIMAL(26,8) 	,
 	 "FechaIngreso"							 	TIMESTAMP		,
 	 "FechaOperacion" 							SECONDDATE	 );
 	 
 	 
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::AsignacionPaso" 
       START     WITH 2000
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::OrdenPaso" 
       START     WITH 2000
       INCREMENT BY 1
       MINVALUE  1;
	   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::TransaccionPaso" 
       START     WITH 2000
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::AsignacionResultado" 
       START     WITH 2000
       INCREMENT BY 1
       MINVALUE  1;
	   

commit;
