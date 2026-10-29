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
--       <MMACHMAR>.<LIBRO ORDENES>
--################################################

DROP TABLE "BSCL.Orden.BD::ORD.ProcesoAsignacionAutomatica";

-- TABLE : Libro de Ordenes
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenesAUX";
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenesAUX" (
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
	"IdTipoBloqueo"					NVARCHAR(40)	NOT NULL DEFAULT '',
	"FechaBloqueo"					SECONDDATE		NOT NULL DEFAULT '0001-01-01 00:00:00',
	"ComentarioBloqueo"				NVARCHAR(520)	NOT NULL DEFAULT '',
	"IdTipoDesbloqueo"				NVARCHAR(40)	NOT NULL DEFAULT '',
	"FechaDesbloqueo"				SECONDDATE  	NOT NULL DEFAULT '0001-01-01 00:00:00',
	"ComentarioDesbloqueo"			NVARCHAR(520)	NOT NULL DEFAULT '',
	"Borrado"						NVARCHAR(1)		NOT NULL DEFAULT 'N',
	PRIMARY KEY ("IdLibroOrdenes"));


--------------------------------------------------
--       FIN LIBRO ORDENES
--------------------------------------------------





