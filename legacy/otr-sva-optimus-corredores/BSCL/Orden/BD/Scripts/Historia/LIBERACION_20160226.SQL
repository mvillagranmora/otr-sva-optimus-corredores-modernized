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
-- LIBERACION SEMANA: 22-02-2016
--

--===========================================================

alter table "BSCL.Orden.BD::ORD.Orden" DROP ("BloqueoAsignacionAutomatica");
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
	--"PorcentajeAsignacion"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	--"EsAPlazo"							NVARCHAR(1) DEFAULT 'N' NOT NULL,
	--"EsOD"								NVARCHAR(1) NOT NULL DEFAULT 'N',
	PRIMARY KEY ("IdDetalleSimulacion")
);

---MANTENEDOR CLIENTE-GRUPO - CHERNADNEZ

CREATE COLUMN TABLE "BSCL.Orden.BD::ORD.Grupo" (
   "IdGrupo"  				BIGINT              not null,
   "IdEmpresa"				BIGINT				not null,
   "Nombre"         		NVARCHAR(50)        not null,
   "IdEstado"       		NVARCHAR(3)         not null,
   PRIMARY KEY("IdGrupo")
);


CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.GrupoPortafolio" (
   "IdGrupoPortafolio"		BIGINT              not null,
   "IdEmpresa"				BIGINT				not null,
   "IdGrupo"         		BIGINT        		not null,
   "IdPortafolio"      		BIGINT		    	not null,
   PRIMARY KEY("IdGrupoPortafolio")
);

--Secuencia tabla Grupo
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::Grupo"
   START     WITH 1
   INCREMENT BY 1
   MINVALUE  1; 
   
CREATE SEQUENCE "BSCL.Orden.BD.Secuencias::GrupoPortafolio"
   START     WITH 1
   INCREMENT BY 1
   MINVALUE  1;  

INSERT INTO "BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
VALUES(10, 'ACT', 'GRUPOPORTAFOLIO', 'Activo');
INSERT INTO "BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
VALUES(11, 'INA', 'GRUPOPORTAFOLIO', 'Inactivo');
