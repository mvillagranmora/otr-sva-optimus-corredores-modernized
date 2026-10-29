SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

-- EstadosPerfilComercial
drop table "BSCL.Plataforma.BD::COM.EstadoPerfilComercial"				cascade;
-- Productos
drop table "BSCL.Plataforma.BD::COM.DetallePerfilProductos" 			cascade;
drop table "BSCL.Plataforma.BD::COM.PerfilProductos" 					cascade;

-- Cobros
drop table "BSCL.Plataforma.BD::COM.Cobro" 								cascade;
drop table "BSCL.Plataforma.BD::COM.CobroClasificacion" 				cascade;
drop table "BSCL.Plataforma.BD::COM.DetallePerfilCobros" 				cascade;
drop table "BSCL.Plataforma.BD::COM.PerfilCobros" 						cascade;
drop table "BSCL.Plataforma.BD::COM.ValorCobro"							cascade;

-- Perfil Comercial Base
drop table "BSCL.Plataforma.BD::COM.PerfilComercialBase" 				cascade;

-- Categoria Inversionista
drop table "BSCL.Plataforma.BD::COM.CategoriaInversionista"				cascade;

-- Grupos de Activos
drop table "BSCL.Plataforma.BD::COM.GrupoActivos" 						cascade;
drop table "BSCL.Plataforma.BD::COM.DetalleGrupoActivos"				cascade;

-- Plantilla de Agente
DROP TABLE "BSCL.Plataforma.BD::COM.PlantillaEmpleado"					cascade;
DROP TABLE "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado"			cascade;
 
/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

/*************************
 ESTADO PERFIL COMERCIAL
*************************/
create column table "BSCL.Plataforma.BD::COM.EstadoPerfilComercial" (
	"IdEstadoPerfilComercial"			BIGINT				not null,
	"IdEstado"   						NVARCHAR(3)       	not null,
	"Entidad"         					NVARCHAR(40)       	not null,
	primary key ("IdEstadoPerfilComercial")
);


/*************************
 CATEGORIA INVERSIONISTA 
*************************/

create column table "BSCL.Plataforma.BD::COM.CategoriaInversionista" (
	"IdCategoriaInversionista"			NVARCHAR(40)		not null,
	"NombreCategoriaInversionista"   	NVARCHAR(100)       not null,
	"IdEmpresa"         				BIGINT              not null,
	primary key ("IdCategoriaInversionista")
);

/*************************
   PERFIL COMERCIAL BASE  
*************************/

create column table "BSCL.Plataforma.BD::COM.PerfilComercialBase" ( 
	"IdPerfilComercialBase"				NVARCHAR(40)		not null,
	"DescripcionPerfilComercialBase"	NVARCHAR(100)		not null,
	"IdCategoriaInversionista"			NVARCHAR(40)		not null,
	/*Perfiles*/
	"IdPerfilProductos"					NVARCHAR(40)		not null,
	"IdPerfilCobros"					NVARCHAR(40)		not null,
	"IdPerfilLimites"					NVARCHAR(40)		not null,
	"IdPerfilCondicionesOperativas"		NVARCHAR(40)		not null,
	"IdModeloFacturacion"				NVARCHAR(40)		not null,
	--"IdPerfilFormasPagoPermitidos"		NVARCHAR(40)		not null,
	/*--------*/
	"FechaCreacion"						DATE				not null,
	"FechaModificacion"					DATE				not null,
	"IdEstado"					   		NVARCHAR(3)    		not null,
	"PorDefecto"						NVARCHAR(1)    		not null,
	"IdEmpresa"							BIGINT				not null,
	primary key ("IdPerfilComercialBase")	
);

/*************************
	PERFIL PRODUCTOS 
*************************/

create column table "BSCL.Plataforma.BD::COM.PerfilProductos" (
	"IdPerfilProductos"					NVARCHAR(40)		not null,
	"DescripcionPerfilProductos"		NVARCHAR(100)		not null,
	"IdCategoriaInversionista"			NVARCHAR(40)		not null,
	"EsPerfilPersonalizado"				NVARCHAR(1)			not null,
	"TipoEntidadReferencia"				NVARCHAR(40)		not null default '',
	"IdReferencia"						BIGINT				not null default 0,
	"FechaCreacion"						DATE				not null,
	"FechaModificacion"					DATE				not null,
	"FechaIngreso" 						SECONDDATE CS_SECONDDATE	 DEFAULT '0001-01-01 00:00:00' NOT NULL ,
	"IdEstadoAutorizacion"				NVARCHAR(3) 				DEFAULT '' NOT NULL ,--*
	"IdUsuarioCreador"  				BIGINT 						 DEFAULT 0,
	"IdEstado"					   		NVARCHAR(3)			not null,
	"PorDefecto"						NVARCHAR(1)			not null,
	"IdEmpresa"							BIGINT				not null,
	primary key ("IdPerfilProductos")
);

create column table "BSCL.Plataforma.BD::COM.DetallePerfilProductos" (
	"IdDetallePerfilProductos"			BIGINT              not null, 
	"IdPerfilProductos"					NVARCHAR(40)		not null 	default '',
	"IdTipoProducto"					NVARCHAR(4)         not null 	default '', 
	"IdTipoOperacion"					NVARCHAR(6)         not null 	default '',
	"IdCanalIngreso"					VARCHAR(3)          not null 	default '', 
	"IdMoneda"							VARCHAR(3)          not null 	default '', 
	"IdGrupoActivos"					NVARCHAR(40)		not null 	default '',
	"FechaCreacion"						DATE				not null 	default '0001-01-01 00:00:00',
	"FechaIngreso" 						SECONDDATE CS_SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL ,
	"FechaModificacion" 				SECONDDATE CS_SECONDDATE 		DEFAULT '0001-01-01 00:00:00' NOT NULL ,
	"IdEstadoAutorizacion"				NVARCHAR(3) 					DEFAULT '' NOT NULL ,--*
	"IdUsuarioCreador"  				BIGINT  						DEFAULT 0	
	"IdEstado"					   		NVARCHAR(3)       	not null 	default '',
	"IdEmpresa"							BIGINT              not null 	default 0,
	primary key ("IdDetallePerfilProductos")
);

--ALTER TABLE "BSCL.Plataforma.BD::COM.DetallePerfilProductos" 
--ADD ("IdGrupoActivos" NVARCHAR(40) not null default '');


/*************************
	PERFIL COBROS 
*************************/

create column table "BSCL.Plataforma.BD::COM.PerfilCobros" ( 
	"IdPerfilCobros"					NVARCHAR(40)		not null,
	"DescripcionPerfilCobros"			NVARCHAR(100)       not null,
	"IdCategoriaInversionista"			BIGINT				not null,
	"EsPerfilPersonalizado"				NVARCHAR(1)       	not null,
	"TipoEntidadReferencia"				NVARCHAR(40)		not null default '',
	"IdReferencia"						BIGINT				not null default 0,
	"FechaCreacion"						DATE				not null,
	"FechaModificacion"					DATE				not null,
	"FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
	"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
	"IdUsuarioCreador"  				BIGINT  DEFAULT 0,
	"IdEstado"					   		NVARCHAR(3)       	not null,
	"PorDefecto"						NVARCHAR(1)       	not null,
	"IdEmpresa"							BIGINT              not null,
	primary key ("IdPerfilCobros")
);

alter table "BSCL.Plataforma.BD::COM.PerfilCobros" 
drop ("IdCategoriaInversionista");

create column table "BSCL.Plataforma.BD::COM.DetallePerfilCobros" (
	"IdDetallePerfilCobros"				BIGINT              not null, 
	"IdPerfilCobros"					NVARCHAR(40)        not null, 
	"IdValorCobro"						NVARCHAR(40)        not null, 
	"IdBolsa"							NVARCHAR(10)        not null, 
	"IdTipoProducto"					NVARCHAR(4)         not null, 
	"IdTipoOperacion"					NVARCHAR(6)         not null, 
	"IdCanalIngreso"					VARCHAR(3)          not null, 
	"IdMoneda"							VARCHAR(3)          not null,
	"DiasPlazo"							BIGINT				not null, 
	"Prioridad"							BIGINT				not null,
	"FechaCreacion"						DATE				not null, 
	"FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 	"FechaModificacion" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
 	"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
 	"IdUsuarioCreador"  				BIGINT  DEFAULT 0,
	"IdEstado"					   		NVARCHAR(3)       	not null, 
	primary key ("IdDetallePerfilCobros")
);

/* Maestro Cobros */
create column table "BSCL.Plataforma.BD::COM.CobroClasificacion" (
	"IdCobroClasificacion"				BIGINT				not null, 
	"CodigoCobroClasificacion"			NVARCHAR(40)		not null, 
	"DescripcionCobroClasificacion"  	NVARCHAR(100)       not null,
	"IdEmpresa"         				BIGINT              not null, 
	primary key ("IdCobroClasificacion")
);

create column table "BSCL.Plataforma.BD::COM.Cobro" (
	"IdCobro"							NVARCHAR(40)		not null,
	"NombreCobro"						NVARCHAR(100)     	not null,
	"IdCobroClasificacion"				BIGINT				not null,
	"FormaCobro"						NVARCHAR(1)			not null, --// V = Variable, F = Fijo
	"ValorCobro"						DECIMAL(26,8) 		not null,	-- si es variable: porcentaje! -> 5 es 5%, si es fijo 5 es 5 pesos/uf etc.
	"ValorTopeMinimoOrden"				DECIMAL(26,8) 		not null,	
	"UnidadDeCobro"						NVARCHAR(10)		not null,
	"PermiteCambiarValor"				NVARCHAR(1)			not null, -- S/N
	"IdEmpresa"         				BIGINT              not null,
	"PermiteAsignarCorredorCorresponsal" NVARCHAR(1) NOT NULL DEFAULT 'N',
	"PermiteAsignarPortafolio" 			NVARCHAR(1) NOT NULL DEFAULT 'S',
	"EsRecalculable"                    NVARCHAR(1)	Default 'N' not null, -- S/N
	primary key ("IdCobro")
);

create column table "BSCL.Plataforma.BD::COM.ValorCobro" (
	"IdValorCobro"						NVARCHAR(40)		not null,
	"DescripcionValorCobro"				NVARCHAR(100)     	not null,
	"IdTipoProducto"					NVARCHAR(4)     	not null,
	"IdCobro"							NVARCHAR(10)     	not null,
	"ValorCobro"						DECIMAL(26,8) 		not null,	-- si es variable: porcentaje! -> 5 es 5%, si es fijo 5 es 5 pesos/uf etc.
	"ValorTopeMinimoOrden"				DECIMAL(26,8) 		not null,	
	"IdMoneda"							NVARCHAR(3)     	not null,
	"FechaCreacion"						DATE				not null, 
	"IdEstado"					   		NVARCHAR(3)       	not null, 
	"IdEmpresa"							BIGINT              not null,
	"EsPersonalizado"					NVARCHAR(1)			not null default 'N',							 
	primary key ("IdValorCobro")
);

/**********************************
GRUPO DE ACTIVOS 
**********************************/

create column table "BSCL.Plataforma.BD::COM.GrupoActivos" (
	"IdGrupoActivos"					NVARCHAR(40)		not null,
	"DescripcionGrupoActivos"			NVARCHAR(100)		not null	default '',
	"IdTipoGrupoActivos"				NVARCHAR(40)		not null 	default '',	-- INCL: Incluyente, EXCL: Excluyente
	"FechaCreacion"						DATE				not null 	default '0001-01-01 00:00:00',
	"FechaModificacion"					DATE				not null	default '0001-01-01 00:00:00',
	"IdEstado"					   		NVARCHAR(3)			not null	default 'ACT',
	"FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
	"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
	"IdUsuarioCreador"  				BIGINT  DEFAULT 0,	
	"PorDefecto"						NVARCHAR(1)			not null	default 'N',
	"IdEmpresa"							BIGINT				not null	default 0,
	primary key ("IdGrupoActivos")
);

create column table "BSCL.Plataforma.BD::COM.DetalleGrupoActivos" (
	"IdDetalleGrupoActivos"				BIGINT              not null, 
	"IdGrupoActivos"					NVARCHAR(40)		not null	default '',
	"IdInstrumento"						BIGINT				not null 	default 0,
	"FechaCreacion"						DATE				not null	default '0001-01-01 00:00:00',
 	"FechaIngreso" 						SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
	"FechaModificacion" 				SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
	"IdEstadoAutorizacion"				NVARCHAR(3) DEFAULT '' NOT NULL ,--*
	"IdUsuarioCreador"  				BIGINT  DEFAULT 0,	
	"IdEstado"					   		NVARCHAR(3)       	not null	default 'ACT',
	"IdEmpresa"							BIGINT              not null	default	0,
	primary key ("IdDetalleGrupoActivos")
);

create column table "BSCL.Plataforma.BD::COM.TipoGrupoActivos" (
	"IdTipoGrupoActivos"				NVARCHAR(40)		not null,
	"Nombre"							NVARCHAR(100)		not null	default '',
	primary key ("IdTipoGrupoActivos")
);


/*************************
	PLANTILLA EMPLEADOS
*************************/
create column table "BSCL.Plataforma.BD::COM.PlantillaEmpleado" ( 
	"IdPlantillaEmpleado"				NVARCHAR(40)		NOT NULL,
	"Descripcion"						NVARCHAR(100)       NOT NULL DEFAULT '',
	"FechaCreacion"						DATE				NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"					DATE				NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdEstado"					   		NVARCHAR(3)       	NOT NULL DEFAULT 'ACT',
	--"PorDefecto"						NVARCHAR(1)       	NOT NULL DEFAULT ,
	"IdEmpresa"							BIGINT              NOT NULL DEFAULT 0,
	"IdUsuarioCreador"					BIGINT				NOT NULL DEFAULT 0,
	primary key ("IdPlantillaEmpleado")
);

create column table "BSCL.Plataforma.BD::COM.DetallePlantillaEmpleado" (
	"IdDetallePlantillaEmpleado"		BIGINT              NOT NULL, 
	"IdPlantillaEmpleado"				NVARCHAR(40)        NOT NULL DEFAULT '', 
	"IdTipoProducto"					NVARCHAR(4)         NOT NULL DEFAULT '', 
	"IdTipoOperacion"					NVARCHAR(6)         NOT NULL DEFAULT '', 
	"MontoMinimo"						DECIMAL(26,8) 		NOT NULL DEFAULT 0,
	"MontoMaximo"						DECIMAL(26,8) 		NOT NULL DEFAULT 0,
	"IdMoneda"							NVARCHAR(3)    		NOT NULL DEFAULT '',
	"PorcentajeDescuentoComision"		DECIMAL(26,8) 		NOT NULL DEFAULT 0, -- valor en %
	--
	"PorcentajeMargenEmpleado"			DECIMAL(26,8) 		NOT NULL DEFAULT 0,
	"Ruteo"								NVARCHAR(1)        	NOT NULL DEFAULT 'N', 
	"MontoMaximoRuteo"					DECIMAL(26,8) 		NOT NULL DEFAULT 0,
	"OrdenDirecta"						NVARCHAR(1)        	NOT NULL DEFAULT 'N', 
	"FechaCreacion"						DATE				NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdEstado"					   		NVARCHAR(3)       	NOT NULL DEFAULT 'ACT', 
	primary key ("IdDetallePlantillaEmpleado")
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

--
drop sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaAgente";
create sequence "BSCL.Plataforma.BD.Secuencias::DetallePlantillaAgente" increment by 1 start with 1000 minvalue 1000 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::DetalleGrupoActivos";
create sequence "BSCL.Plataforma.BD.Secuencias::DetalleGrupoActivos" increment by 1 start with 1100 minvalue 1100 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::DetallePerfilCobros";
create sequence "BSCL.Plataforma.BD.Secuencias::DetallePerfilCobros" increment by 1 start with 1100 minvalue 1100 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::DetallePerfilProductos";
create sequence "BSCL.Plataforma.BD.Secuencias::DetallePerfilProductos" increment by 1 start with 1100 minvalue 1100 maxvalue 4611686018427387903 no cycle ;

drop sequence "BSCL.Plataforma.BD.Secuencias::Sucursal";
create sequence "BSCL.Plataforma.BD.Secuencias::Sucursal" increment by 1 start with 1100 minvalue 1100 maxvalue 4611686018427387903 no cycle ;



/*==============================================================*/
/* Tipos:                                                       */
/*==============================================================*/
DROP TYPE "BSCL.Plataforma.BD::COM.Tipos.ConsultarPerfilCobros";
CREATE TYPE "BSCL.Plataforma.BD::COM.Tipos.ConsultarPerfilCobros" AS TABLE ("IdInternal" BIGINT,"Prioridad" BIGINT,"IdPerfilCobros" NVARCHAR(40),"DescripcionPerfilCobros" NVARCHAR(100),"IdEmpresa" BIGINT,"IdCategoriaInversionista" NVARCHAR(40),"EstadoPerfilCobros" NVARCHAR(3),"EsPerfilPersonalizado" NVARCHAR(1),"IdDetallePerfilCobros" BIGINT,"IdBolsa" NVARCHAR(10),"IdTipoProducto" NVARCHAR(4),"IdTipoOperacion" NVARCHAR(6),"IdCanalIngreso" VARCHAR(3),"DiasPlazo" BIGINT,"EstadoDetalle" NVARCHAR(3),"IdValorCobro" NVARCHAR(40),"DescripcionValorCobro" VARCHAR(100),"IdCobro" NVARCHAR(10),"ValorCobro" DECIMAL(26,8),"ValorTopeMinimoOrden" DECIMAL(26,8),"IdMoneda" VARCHAR(3),"EstadoValorCobro" NVARCHAR(3),"NombreCobro" NVARCHAR(100),"IdCobroClasificacion" BIGINT,"FormaCobro" NVARCHAR(1),"PermiteCambiarValor" NVARCHAR(1),"CodigoCobroClasificacion" NVARCHAR(40),"CobroCalculado" INTEGER );

/*DROP TYPE "BSCL.Plataforma.BD::COM.Tipos.MatrizOperacional";
CREATE TYPE "BSCL.Plataforma.BD::COM.Tipos.MatrizOperacional" 
as table (
	"IdBolsa" 					NVARCHAR(10),
	"IdTipoProducto"			NVARCHAR(4),
	"TipoProducto"				NVARCHAR(100),
	"IdTipoOperacion"			NVARCHAR(6),
	"TipoOperacion"				NVARCHAR(100),
	--"ProductoIdTipoOperacion"		NVARCHAR(6),
	--"CobroIdTipoOperacion"		NVARCHAR(6),
	"IdCanalIngreso"			NVARCHAR(4),
	"CanalIngreso"				NVARCHAR(100),
	--"ProductoIdCanalIngreso"		NVARCHAR(4),
	--"CobroIdCanalIngreso"		NVARCHAR(4),
	"IdCobro"					NVARCHAR(10),
	"ValorCobro"				DECIMAL(26,8), -- ej:si es variable 19 es 19% , si es fijo 19 es 19 pesos/uf/etc...
	"FormaCobro"				NVARCHAR(1),
	"ValorTopeMinimoOrden"		DECIMAL(26,8),
	"IdMoneda"					NVARCHAR(3),
	"Prioridad" 				BIGINT,
	"DiasPlazo"					BIGINT,
	"NombreCobro"				NVARCHAR(100),
	"IdCobroClasificacion"		BIGINT,
	"CodigoCobroClasificacion"	NVARCHAR(40),
	"IdEmpresa" 				BIGINT
	);*/
	
	
/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* PerfilProductos: */

--alter table "BSCL.Plataforma.BD::COM.DetalleCondicionOperativa"
--   add constraint "FK_TipoCondicionOperativa_DetalleCondicionOperativa" foreign key ("IdTipoCondicionOperativa")
--      references "BSCL.Plataforma.BD::COM.TipoCondicionOperativa" ("IdTipoCondicionOperativa") on delete restrict on update restrict;
      
/* DetallePerfilProductos: */

/* PerfilProductos: */

/* CobroClasificacion: */

/* Cobro: */



/* PerfilCobros: */

/* DetallePerfilCobros: */

/* LIMITES */

/* TipoCondicionOperativa: */

/* CondicionOperativa: */

/* DetalleCondicionOperativa: */

/* PerfilCondicionesOperativas: */

/* DetallePerfilCondicionesOperativas: */

/* TipoPreferenciaFacturacion: */

/* PreferenciaFacturacion: */

/* DetallePreferenciaFacturacion: */

/* PerfilFacturacion: */

/* DetallePerfilFacturacion: */

/* FormaPago: */

/* PerfilFormasPago: */

/* DetallePerfilFormasPago: */


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

--create index "INX01_Evento" on "BSCL.Plataforma.BD::AUT.Evento"("IdTipoOperacion","IdConcepto","NumeroOperacionConcepto","FechaOperacionConcepto","IdEstado");

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
