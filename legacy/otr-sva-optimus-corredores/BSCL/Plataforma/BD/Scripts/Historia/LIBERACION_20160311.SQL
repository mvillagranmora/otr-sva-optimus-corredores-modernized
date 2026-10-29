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
-- LIBERACION SEMANA: 11-03-2016
--



--################################################
--       JVILLAGRAN - Tesorería
--################################################


-- Nuevos estados para comprobantes de tesorería
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('GIR','Girado');

INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('GIP','Girado Parcialmente');

-- Inscripción de servicio
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(216,'BSCL.Tesoreria.BD.Servicios::ProcesarClearing',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(226,'BSCL.Tesoreria.BD.Servicios::GirarComprobantesEgreso',0,'P','');

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------
--################################################
--       NROJAS - 
--################################################

INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado","Nombre")
VALUES('RCP','Recepcionado');

--################################################
--       MMACHMAR - Nuevos Servicios LibroOrdenes
--################################################

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(217,'BSCL.Orden.BD.Servicios::CrearLibroOrdenesOperadores',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(220,'BSCL.Orden.BD.Servicios::CambiarEstadoLibroOrdenesDetalle',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(221,'BSCL.Orden.BD.Servicios::CrearLibroOrdenesSegmento',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(222,'BSCL.Orden.BD.Servicios::CrearLibroOrdenesMarcasNegociacion',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(223,'BSCL.Orden.BD.Servicios::CrearLibrosPorDefecto',0,'P','');

--------------------------------------------------
--      FIN <Servicios>
--------------------------------------------------


--################################################
--       NROJAS - Limpieza
--################################################
DROP TABLE "BSCL.Plataforma.BD::PAG.FormaDePago" 	 cascade;

--################################################
--       EESPINOZA - Cambio tablas
--################################################
ALTER TABLE "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle"
ADD CONSTRAINT "UNQ01_ConfiguracionEventoDetalle" UNIQUE ("IdConfiguracionEvento","CodigoTipoEvento");

ALTER TABLE "BSCL.Plataforma.BD::EVT.Plantilla" 
ADD ("Descripcion"			NVARCHAR(200) );


--################################################
--       NROJAS - Nomina y Transferencia
--################################################
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(225,'BSCL.Tesoreria.BD.Servicios::RecepcionarNominasTransferencias',0,'P','');


DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::LogMasivo"	CASCADE; 
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LogMasivo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;       
              
--################################################
--       CESPINOSA - Cierre Modulos
--################################################
DROP TABLE "BSCL.Plataforma.BD::PLA.CREModuloEmpresa"  cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso"   cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.CRETipoRequisito"    cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.CREModuloFuncionProcesoDependencia" cascade;

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.CREModuloEmpresa" (
	 "IdCREModuloEmpresa" 		BIGINT CS_FIXED NOT NULL ,
	 "IdEmpresa"				BIGINT CS_FIXED NOT NULL , 	/* viene de tabla "BSCL.Plataforma.BD::PLA.Empresa" */
	 "IdModulo"					NVARCHAR(3) NOT NULL,  		/* viene de tabla "BSCL.Plataforma.BD::PLA.Modulo" */
	 "NombreModuloEmpresa"		NVARCHAR(40) DEFAULT NULL,
	 "TipoApretura"				CHAR(1) DEFAULT NULL, /* [A]utomatico - [M]anual */
	 "HoraApertura"				NVARCHAR(8) DEFAULT NULL,
	 "TipoCierre"				CHAR(1) DEFAULT NULL,  /* [A]utomatico - [M]anual */
	 "HoraCierre"				NVARCHAR(8) DEFAULT NULL,
	 "IdEstado"					NVARCHAR(3) NOT NULL, 		/* viene de tabla "BSCL.Plataforma.BD::PLA.Estado" */
	 PRIMARY KEY ("IdCREModuloEmpresa")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" (
	 "IdModuloFuncionProceso" 	BIGINT CS_FIXED NOT NULL , 
	 "IdCREModuloEmpresa"		BIGINT CS_FIXED NOT NULL , 	/* viene de tabla "BSCL.Plataforma.BD::PLA.CREModuloEmpresa" */
	 "Descripcion"				NVARCHAR(100) NOT NULL ,
	 "NombreFuncionProceso"		NVARCHAR(40) NOT NULL,
	 "PermiteEjecucion"			NVARCHAR(1) DEFAULT NULL, 		/* [A]bierto - [C]errado - [T]odos */
	 "IdEstado"					NVARCHAR(3) NOT NULL, 		/* viene de tabla "BSCL.Plataforma.BD::PLA.Estado" */
	 PRIMARY KEY ("IdModuloFuncionProceso")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.CRETipoRequisito" (
	"IdTipoRequisito"			NVARCHAR(3) NOT NULL ,
	"Nombre"					NVARCHAR(20) NOT NULL,
	"IdEstado"					NVARCHAR(3) NOT NULL, 		/* viene de tabla "BSCL.Plataforma.BD::PLA.Estado" */
	PRIMARY KEY ("IdTipoRequisito")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre" (
	 "IdModuloRequisitoAperturaCierre" 	BIGINT CS_FIXED NOT NULL ,
	 "IdTipoRequisito"					NVARCHAR(3) NOT NULL, 		   /* viene de tabla "BSCL.Plataforma.BD::PLA.CRETipoRequisito" */
	 "IdUsuario" 						BIGINT CS_FIXED DEFAULT NULL , /* viene de tabla "BSCL.Plataforma.BD::ACC.Usuario" */
	 "IdPerfil"       					BIGINT CS_FIXED DEFAULT NULL , /* viene de tabla "BSCL.Plataforma.BD::ACC.Perfil" */
	 "IdModuloFuncionProceso" 			BIGINT CS_FIXED DEFAULT NULL , /* viene de tabla "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" */
	 "Orden"							INT NOT NULL,
	 "Tipo"								CHAR(1) NOT NULL,  /* [A]pertura - [C]ierre */
	 "HoraMinApertura"					NVARCHAR(8) DEFAULT NULL,
	 "HoraMinCierre"					NVARCHAR(8) DEFAULT NULL,
	 "Nombre"							NVARCHAR(40) DEFAULT NULL,
	 "AbreModulo"						CHAR(1) NOT NULL,
	 "CierraModulo"						CHAR(1) NOT NULL,
	 "IdEstado"							NVARCHAR(3) NOT NULL, 			/* viene de tabla "BSCL.Plataforma.BD::PLA.Estado" */
	 PRIMARY KEY ("IdModuloRequisitoAperturaCierre")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.CREModuloFuncionProcesoDependencia" (
	 "IdModuloFuncionProcesoDependencia" BIGINT CS_FIXED NOT NULL ,
	 "IdModuloFuncionProcesoPrincipal"	 BIGINT CS_FIXED NOT NULL , /* viene de tabla "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" */
	 "IdModuloFuncionProceso" 			 BIGINT CS_FIXED NOT NULL , /* viene de tabla "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" */
	 "TipoDependencia"					 NVARCHAR(1) NOT NULL,
	 "IdEstado"							 NVARCHAR(3) NOT NULL, 		/* viene de tabla "BSCL.Plataforma.BD::PLA.Estado" */
	 PRIMARY KEY ("IdModuloFuncionProcesoDependencia")
);


/* Table: "BSCL.Plataforma.BD::PLA.CREModuloEmpresa" */

alter table "BSCL.Plataforma.BD::PLA.CREModuloEmpresa"
add constraint "FK_Empresa_CREModuloEmpresa" foreign key ("IdEmpresa")
references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloEmpresa"
add constraint "FK_Modulo_CREModuloEmpresa" foreign key ("IdModulo")
references "BSCL.Plataforma.BD::PLA.Modulo" ("IdModulo") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloEmpresa"
add constraint "FK_Estado_CREModuloEmpresa" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" */
alter table "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso"
add constraint "FK_CREModuloEmpresa_CREModuloFuncionProceso" foreign key ("IdCREModuloEmpresa")
references "BSCL.Plataforma.BD::PLA.CREModuloEmpresa" ("IdCREModuloEmpresa") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso"
add constraint "FK_Estado_CREModuloFuncionProceso" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: "BSCL.Plataforma.BD::PLA.CRETipoRequisito" */
alter table "BSCL.Plataforma.BD::PLA.CRETipoRequisito"
add constraint "FK_Estado_RETipoRequisito" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre" */
alter table "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre"
add constraint "FK_CRETipoRequisito_CREModuloRequisitoAperturaCierre" foreign key ("IdTipoRequisito")
references "BSCL.Plataforma.BD::PLA.CRETipoRequisito" ("IdTipoRequisito") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre"
add constraint "FK_Usuario_CREModuloRequisitoAperturaCierre" foreign key ("IdUsuario")
references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre"
add constraint "FK_Perfil_CREModuloRequisitoAperturaCierre" foreign key ("IdPerfil")
references "BSCL.Plataforma.BD::ACC.Perfil" ("IdPerfil") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre"
add constraint "FK_CREModuloFuncionProceso_CREModuloRequisitoAperturaCierre" foreign key ("IdModuloFuncionProceso")
references "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" ("IdModuloFuncionProceso") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloRequisitoAperturaCierre"
add constraint "FK_Estado_CREModuloRequisitoAperturaCierre" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: "BSCL.Plataforma.BD::PLA.CREModuloFuncionProcesoDependencia" */
alter table "BSCL.Plataforma.BD::PLA.CREModuloFuncionProcesoDependencia"
add constraint "FK_CREModuloFuncionProcesoPrincipal_CREModuloFuncionProcesoDependencia" foreign key ("IdModuloFuncionProcesoPrincipal")
references "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" ("IdModuloFuncionProceso") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloFuncionProcesoDependencia"
add constraint "FK_CREModuloFuncionProceso_CREModuloFuncionProcesoDependencia" foreign key ("IdModuloFuncionProceso")
references "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" ("IdModuloFuncionProceso") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::PLA.CREModuloFuncionProcesoDependencia"
add constraint "FK_Estado_CREModuloFuncionProcesoDependencia" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

INSERT INTO "BSCL.Plataforma.BD::PLA.CRETipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado")
VALUES ('USU', 'Usuario', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.CRETipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado")
VALUES ('PRF', 'Perfil', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.CRETipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado")
VALUES ('PRO', 'Procesos y Funciones', 'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.CRETipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado")
VALUES ('HOR', 'Horario', 'ACT');

