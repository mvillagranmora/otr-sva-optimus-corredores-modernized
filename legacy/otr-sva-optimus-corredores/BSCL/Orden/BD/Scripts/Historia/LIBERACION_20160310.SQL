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
-- LIBERACION SEMANA: 04-03-2016
--



--################################################
--       <INICIALES DESARROLLADOR>.<MOTIVO>
--################################################

CREATE COLUMN TABLE 
	"BSCL.Orden.BD::ORD.TipoAsignacion" (
            "IdTipoAsignacion"         NVARCHAR(40) NOT NULL,
            "Nombre"					NVARCHAR(100) NOT NULL, 
            "IdEstado"					NVARCHAR(3) NOT NULL,
            "SoportaAsignacionAutomatica" NVARCHAR(1),
            PRIMARY KEY ("IdTipoAsignacion")
            );

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" (
	"IdTipoAsignacionClasificacionTipoInstrumento"	BIGINT,
	"IdTipoAsignacion"         						NVARCHAR(40) NOT NULL,
	"EsContraOrden"									NVARCHAR(1) NOT NULL, --si es N, es contra cliente
	"IdClasificacionTipoInstrumento"				NVARCHAR(2) NOT NULL,
	PRIMARY KEY ("IdTipoAsignacionClasificacionTipoInstrumento")
);
-- INSERT tipo Proceso Asignacion Automatica
insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_POSTRERA','UI_ASIGNACION_POSTRERA','ACT','N');
insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_PROPORCIONAL','UI_ASIGNACION_PROPORCIONAL','ACT','S');
insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_CRONOLOGICA','UI_ASIGNACION_CRONOLOGICA','ACT','S');
insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_PRECIO_PROMEDIO','UI_ASIGNACION_PRECIO_PROMEDIO','ACT','N');
insert into "BSCL.Orden.BD::ORD.TipoAsignacion"("IdTipoAsignacion","Nombre","IdEstado","SoportaAsignacionAutomatica") 
values ('ASIGNACION_MANUAL','UI_ASIGNACION_MANUAL','ACT','N');

insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (1,'ASIGNACION_MANUAL','S','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (2,'ASIGNACION_MANUAL','S','DV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (3,'ASIGNACION_CRONOLOGICA','S','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (4,'ASIGNACION_CRONOLOGICA','S','DV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (5,'ASIGNACION_PROPORCIONAL','S','RV');

insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (6,'ASIGNACION_PRECIO_PROMEDIO','N','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (7,'ASIGNACION_POSTRERA','N','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (8,'ASIGNACION_POSTRERA','N','RV');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (9,'ASIGNACION_POSTRERA','N','RF');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (10,'ASIGNACION_POSTRERA','N','IF');
insert into "BSCL.Orden.BD::ORD.TipoAsignacionClasificacionTipoInstrumento" 
values (11,'ASIGNACION_POSTRERA','N','DV');


--################################################
--       <MMACHMAR>.<LIBRO ORDENES>
--################################################

DROP TABLE "BSCL.Orden.BD::ORD.ProcesoAsignacionAutomatica";

-- TABLE : Libro de Ordenes
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenesAUX";
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenesAUX" (
	"IdLibroOrdenes"				BIGINT			NOT NULL,
	"IdEmpresa"						BIGINT			NOT NULL DEFAULT 0 UNIQUE,
	"NombreLibroOrdenes"			NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdTipoLibroOrden"				NVARCHAR(6)		NOT NULL DEFAULT '',
	"IdEstado"						NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"AsignacionAutomatica"			NVARCHAR(2)		NOT NULL DEFAULT 'NO',
	"IdTipoAsignacion"				NVARCHAR(40)	NOT NULL DEFAULT '',
	"Prioridad"						BIGINT			NOT NULL DEFAULT 0,
	"LibroPorDefecto"				NVARCHAR(2)		UNIQUE,
	"FechaCreacion"					DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"				DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdTipoBloqueo"					NVARCHAR(40)	NOT NULL DEFAULT '',
	"FechaBloqueo"					SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"ComentarioBloqueo"				NVARCHAR(520)	NOT NULL DEFAULT '',
	"IdTipoDesbloqueo"				NVARCHAR(40)	NOT NULL DEFAULT '',
	"FechaDesbloqueo"				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"ComentarioDesbloqueo"			NVARCHAR(520)	NOT NULL DEFAULT '',
	"Borrado"						NVARCHAR(1)		NOT NULL DEFAULT 'N',
	PRIMARY KEY ("IdLibroOrdenes"));


-- TABLA LibroOrdenesOperador
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenesOperador";
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenesOperador" (
	"IdLibroOrdenesOperador"	BIGINT			NOT NULL,
	"IdEmpresa"					BIGINT			NOT NULL DEFAULT 0,
	"IdLibroOrdenes"			BIGINT			NOT NULL DEFAULT 0,
	"CodigoOperador"			NVARCHAR(10)	NOT NULL DEFAULT '',
	"IdEstado"					NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"FechaCreacion"				DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"			DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	PRIMARY KEY ("IdLibroOrdenesOperador"));

DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesOperador";	
CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesOperador"
	START WITH 1 
	INCREMENT BY 1
	MINVALUE 1;


-- TABLA LibroOrdenesSegmento
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenesSegmento";
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenesSegmento" (
	"IdLibroOrdenesSegmento"	BIGINT			NOT NULL,
	"IdEmpresa"					BIGINT			NOT NULL DEFAULT 0,
	"IdLibroOrdenes"			BIGINT			NOT NULL DEFAULT 0,
	"IdSegmentoCliente"			BIGINT			NOT NULL DEFAULT 0,
	"IdEstado"					NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"FechaCreacion"				DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"			DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	PRIMARY KEY ("IdLibroOrdenesSegmento"));
	
DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesSegmento";
CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesSegmento" 
	START WITH 1 
	INCREMENT BY 1 
	MINVALUE 1;


-- TABLA LibroOrdenesMarcaNegociacion
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion";
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenesMarcaNegociacion" (
	"IdMarcaNegociacion"	BIGINT			NOT NULL,
	"IdEmpresa"				BIGINT			NOT NULL DEFAULT 0,
	"IdLibroOrdenes"		BIGINT			NOT NULL DEFAULT 0,
	"MarcaNegociacion"		NVARCHAR(520) 	NOT NULL DEFAULT '',
	"IdEstado"				NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"FechaCreacion"			DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"		DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	PRIMARY KEY ("IdMarcaNegociacion"));

DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesMarcaNegociacion";
CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::LibroOrdenesMarcaNegociacion" 
	START WITH 1 
	INCREMENT BY 1 
	MINVALUE 1;
	
--------------------------------------------------
--       FIN Tablas y Secuencias
--------------------------------------------------





