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
-- LIBERACION SEMANA: 25-03-2016
--
drop table "BSCL.Orden.BD::ORD.LibroOrdenesAUX";
drop table "BSCL.Orden.BD::ORD.LibroOrdenes";
drop table "BSCL.Orden.BD::ORD.LibroOrdenesDefinicion";

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenes" (
	"IdLibroOrdenes"				BIGINT			NOT NULL UNIQUE,
	"IdEmpresa"						BIGINT			NOT NULL DEFAULT 0,
	"NombreLibroOrdenes"			NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdTipoLibroOrden"				NVARCHAR(6)		NOT NULL DEFAULT '',
	"IdEstado"						NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"AsignacionAutomatica"			NVARCHAR(2)		NOT NULL DEFAULT 'NO',
	"IdTipoAsignacion"				NVARCHAR(40)	NOT NULL DEFAULT '',
	"Prioridad"						BIGINT			NOT NULL DEFAULT 0,
	"LibroPorDefecto"				NVARCHAR(2)		UNIQUE,
	"FechaCreacion"					DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"				DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"UltimaFechaDeEjecucion" 		SECONDDATE 		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"IdTipoBloqueo"					NVARCHAR(40)	NOT NULL DEFAULT '',
	"FechaBloqueo"					SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"ComentarioBloqueo"				NVARCHAR(520)	NOT NULL DEFAULT '',
	"IdTipoDesbloqueo"				NVARCHAR(40)	NOT NULL DEFAULT '',
	"FechaDesbloqueo"				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"ComentarioDesbloqueo"			NVARCHAR(520)	NOT NULL DEFAULT '',
	"Borrado"						NVARCHAR(1)		NOT NULL DEFAULT 'N',
	PRIMARY KEY ("IdLibroOrdenes"));


UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" set "RutaServicio" = 'BSCL.Orden.BD.Servicios::CrearLibroOrdenes' where "IdServicio" =  211;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" set "RutaServicio" = 'BSCL.Orden.BD.Servicios::ModificarLibroOrdenes' where "IdServicio" =  212;
UPDATE "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" set "RutaServicio" = 'BSCL.Orden.BD.Servicios::CambiarEstadoLibroOrdenes' where "IdServicio" =  213;

alter table "BSCL.Orden.BD::ORD.Asignacion" alter ("IdEstadoFacturacion" NVARCHAR(40) NOT NULL DEFAULT '');

alter table "BSCL.Orden.BD::ORD.CobroOrden" add ("ValorCobroPerfil" DECIMAL(26,8) NOT NULL DEFAULT 0);
alter table "BSCL.Orden.BD::ORD.CobroOrden" add ("CodigoCobroClasificacion" NVARCHAR(40) NULL);
alter table "BSCL.Orden.BD::ORD.CobroOrden" drop ("ValorCobro");


DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarProcesoAsignacionAutomatica";

--------------------------------------------------
--       FIN LIBRO ORDENES
--------------------------------------------------

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(263,'BSCL.Orden.BD.Servicios::CuadraturaCierreDiario',0,'P','');
