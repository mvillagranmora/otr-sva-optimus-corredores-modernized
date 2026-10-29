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

DROP TABLE "BSCL.Orden.BD::ORD.DetalleSimulacionAsignacion" CASCADE;
CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.DetalleSimulacionAsignacion"(
	"IdDetalleSimulacion"				BIGINT NOT NULL,
	"IdSimulacion"						BIGINT NOT NULL,--FK tabla SimulacionAsignacion
	"FechaSimulacion"                   TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
	"FechaIngresoRegistro"              TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
	"IdTransaccion"                     BIGINT NOT NULL,
	"IdOrden"                           BIGINT NOT NULL DEFAULT 0,
	--"IdSocioNegocio"					BIGINT NOT NULL DEFAULT 0,
	"IdPortafolio"						BIGINT NOT NULL DEFAULT 0,
	"IdLibroOrdenes" 					BIGINT NOT NULL,
	"CantidadAsignacion"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"PorcentajeAsignacion"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"MontoAsignacion"					DECIMAL(26,8) NOT NULL DEFAULT 0,
	"ValorRescate"						DECIMAL(26,8) NOT NULL DEFAULT 0,
	"PrecioPromedio"					DECIMAL(26,8) NOT NULL DEFAULT 0,
	--"EsAPlazo"							NVARCHAR(1) DEFAULT 'N' NOT NULL,
	--"EsOD"								NVARCHAR(1) NOT NULL DEFAULT 'N',
	PRIMARY KEY ("IdDetalleSimulacion")
);

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------



--################################################
--       <JVILLAGRAN>.<LIQUIDACION TESORERIA>
--################################################

-- Agrega campo "IdComprobanteTesoreria" a tabla Transaccion. Registrará el ID del comprobante de tesoría cuando se liquide.
ALTER TABLE "BSCL.Orden.BD::ORD.Transaccion"
ADD ( "IdComprobanteTesoreria"	  BIGINT 	NOT NULL DEFAULT 0);   -- No tiene FK ya que si no está liquidado puede estar en cero.

--------------------------------------------------
--      FIN <LIQUIDACION TESORERIA>
--------------------------------------------------


--################################################
--       <MMACHMAR>.<LIBRO ORDENES>
--################################################

-- INSERT de estados del Libro de Ordenes
insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") 
select max("IdEstadoOrden")+1,'ACT','Libro.Ordenes' from "BSCL.Orden.BD::ORD.EstadoOrden";

insert into "BSCL.Orden.BD::ORD.EstadoOrden"("IdEstadoOrden","IdEstado","NombreEntidad") 
select max("IdEstadoOrden")+1,'INA','Libro.Ordenes' from "BSCL.Orden.BD::ORD.EstadoOrden";

-- TABLE : ProcesoAsignacionAutomatica
DROP TABLE "BSCL.Orden.BD::ORD.ProcesoAsignacionAutomatica";

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.ProcesoAsignacionAutomatica" (
	"IdProcesoAsignacionAutomatica"	NVARCHAR(6) NOT NULL,
	"Nombre"						NVARCHAR(100) NOT NULL, PRIMARY KEY ("IdProcesoAsignacionAutomatica"));

-- INSERT tipo Proceso Asignacion Automatica
insert into "BSCL.Orden.BD::ORD.ProcesoAsignacionAutomatica"("IdProcesoAsignacionAutomatica","Nombre") values ('CRONOL','Cronológica');
insert into "BSCL.Orden.BD::ORD.ProcesoAsignacionAutomatica"("IdProcesoAsignacionAutomatica","Nombre") values ('PROPOR','Proporcional');


-- TABLE : Tipo Libro de Ordenes
DROP TABLE "BSCL.Orden.BD::ORD.TipoLibroOrden";

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.TipoLibroOrden" (
	"IdTipoLibroOrden"		NVARCHAR(6) NOT NULL,
	"NombreTipoLibroOrden"	NVARCHAR(100) NOT NULL, PRIMARY KEY ("IdTipoLibroOrden"));
	
-- INSERT Tipo Libro Ordenes
insert into "BSCL.Orden.BD::ORD.TipoLibroOrden"("IdTipoLibroOrden","NombreTipoLibroOrden") values ('NORMAL','Normal');
insert into "BSCL.Orden.BD::ORD.TipoLibroOrden"("IdTipoLibroOrden","NombreTipoLibroOrden") values ('OPA','OPA');
insert into "BSCL.Orden.BD::ORD.TipoLibroOrden"("IdTipoLibroOrden","NombreTipoLibroOrden") values ('REMATE','Remate');
insert into "BSCL.Orden.BD::ORD.TipoLibroOrden"("IdTipoLibroOrden","NombreTipoLibroOrden") values ('COLOCA','Colocación');

-- TABLE : Libro de Ordenes
DROP TABLE "BSCL.Orden.BD::ORD.LibroOrdenesAUX";

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.LibroOrdenesAUX" (
	"IdLibroOrdenes"				BIGINT			NOT NULL,
	"IdEmpresa"						BIGINT			NOT NULL DEFAULT 0,
	"IdOperador"					BIGINT			NOT NULL DEFAULT 0,
	"NombreLibroOrdenes"			NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdBolsa"						NVARCHAR(3)		NOT NULL DEFAULT '',
	"IdTipoLibroOrden"				NVARCHAR(6)		NOT NULL DEFAULT '',
	"IdEstado"						NVARCHAR(3) 	NOT NULL DEFAULT 'ACT',
	"AsignacionAutomatica"			NVARCHAR(2)		NOT NULL DEFAULT 'NO',
	"IdProcesoAsignacionAutomatica"	NVARCHAR(6)		NOT NULL DEFAULT '',
	"Prioridad"						BIGINT			NOT NULL DEFAULT 0,
	"LibroPorDefecto"				NVARCHAR(2)		NOT NULL DEFAULT 'SI',
	"FechaCreacion"					DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	"FechaModificacion"				DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',
	PRIMARY KEY ("IdLibroOrdenes"));

--------------------------------------------------
--      FIN <LIBRO ORDENES>
--------------------------------------------------

