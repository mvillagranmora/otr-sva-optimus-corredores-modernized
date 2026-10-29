SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

DROP TABLE "BSCL.Plataforma.BD::CRE.ModuloEmpresa"  cascade;
DROP TABLE "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso"  cascade;
DROP TABLE "BSCL.Plataforma.BD::CRE.TipoRequisito"  cascade;
DROP TABLE "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"  cascade;
DROP TABLE "BSCL.Plataforma.BD::CRE.ModuloFuncionProcesoDependencia"  cascade;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
CREATE COLUMN TABLE "BSCL.Plataforma.BD::CRE.ModuloEmpresa" (
	 "IdCREModuloEmpresa" 		BIGINT CS_FIXED NOT NULL ,
	 "IdEmpresa"				BIGINT CS_FIXED NOT NULL , 	/* viene de tabla "BSCL.Plataforma.BD::PLA.Empresa" */
	 "IdModulo"					NVARCHAR(3) NOT NULL,  		/* viene de tabla "BSCL.Plataforma.BD::PLA.Modulo" */
	 "NombreModuloEmpresa"		NVARCHAR(40) DEFAULT NULL,
	 "TipoApertura"				CHAR(1) DEFAULT NULL, /* [A]utomatico - [M]anual */
	 "HoraApertura"				NVARCHAR(8) DEFAULT NULL,
	 "TipoCierre"				CHAR(1) DEFAULT NULL,  /* [A]utomatico - [M]anual */
	 "HoraCierre"				NVARCHAR(8) DEFAULT NULL,
	 "IdEstado"					NVARCHAR(3) NOT NULL, 		/* viene de tabla "BSCL.Plataforma.BD::PLA.Estado" */
	 "FechaProceso"				NVARCHAR(10) ,
	 "TipoEstado"				CHAR(1) DEFAULT 'C', 
	 PRIMARY KEY ("IdCREModuloEmpresa")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" (
	 "IdModuloFuncionProceso" 	BIGINT CS_FIXED NOT NULL , 
	 "IdCREModuloEmpresa"		BIGINT CS_FIXED NOT NULL , 	/* viene de tabla "BSCL.Plataforma.BD::PLA.CREModuloEmpresa" */
	 "Descripcion"				NVARCHAR(100) NOT NULL ,
	 "NombreFuncionProceso"		NVARCHAR(100) NOT NULL,
	 "PermiteEjecucion"			NVARCHAR(1) DEFAULT NULL, 		/* [A]bierto - [C]errado - [T]odos */
	 "IdEstado"					NVARCHAR(3) NOT NULL, 		/* viene de tabla "BSCL.Plataforma.BD::PLA.Estado" */
	 PRIMARY KEY ("IdModuloFuncionProceso")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::CRE.TipoRequisito" (
	"IdTipoRequisito"			NVARCHAR(3) NOT NULL ,
	"Nombre"					NVARCHAR(20) NOT NULL,
	"IdEstado"					NVARCHAR(3) NOT NULL, 		/* viene de tabla "BSCL.Plataforma.BD::PLA.Estado" */
	PRIMARY KEY ("IdTipoRequisito")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre" (
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
	 "IdCREModuloEmpresa"				BIGINT NOT NULL,  /* FOREIGN*/
	 PRIMARY KEY ("IdModuloRequisitoAperturaCierre")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::CRE.ModuloFuncionProcesoDependencia" (
	 "IdModuloFuncionProcesoDependencia" BIGINT CS_FIXED NOT NULL ,
	 "IdModuloFuncionProcesoPrincipal"	 BIGINT CS_FIXED NOT NULL , /* viene de tabla "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" */
	 "IdModuloFuncionProceso" 			 BIGINT CS_FIXED NOT NULL , /* viene de tabla "BSCL.Plataforma.BD::PLA.CREModuloFuncionProceso" */
	 "TipoDependencia"					 NVARCHAR(1) NOT NULL,
	 "IdEstado"							 NVARCHAR(3) NOT NULL, 		/* viene de tabla "BSCL.Plataforma.BD::PLA.Estado" */
	 PRIMARY KEY ("IdModuloFuncionProcesoDependencia")
);

/* Table: "BSCL.Plataforma.BD::CRE.ModuloEmpresa" */

alter table "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
add constraint "FK_Empresa_ModuloEmpresa" foreign key ("IdEmpresa")
references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
add constraint "FK_Modulo_ModuloEmpresa" foreign key ("IdModulo")
references "BSCL.Plataforma.BD::PLA.Modulo" ("IdModulo") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
add constraint "FK_Estado_ModuloEmpresa" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" */
alter table "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso"
add constraint "FK_ModuloEmpresa_ModuloFuncionProceso" foreign key ("IdCREModuloEmpresa")
references "BSCL.Plataforma.BD::CRE.ModuloEmpresa" ("IdCREModuloEmpresa") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso"
add constraint "FK_Estado_ModuloFuncionProceso" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: "BSCL.Plataforma.BD::CRE.TipoRequisito" */
alter table "BSCL.Plataforma.BD::CRE.TipoRequisito"
add constraint "FK_Estado_TipoRequisito" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre" */
alter table "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
add constraint "FK_TipoRequisito_ModuloRequisitoAperturaCierre" foreign key ("IdTipoRequisito")
references "BSCL.Plataforma.BD::CRE.TipoRequisito" ("IdTipoRequisito") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
add constraint "FK_Usuario_ModuloRequisitoAperturaCierre" foreign key ("IdUsuario")
references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
add constraint "FK_Perfil_ModuloRequisitoAperturaCierre" foreign key ("IdPerfil")
references "BSCL.Plataforma.BD::ACC.Perfil" ("IdPerfil") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
add constraint "FK_ModuloFuncionProceso_ModuloRequisitoAperturaCierre" foreign key ("IdModuloFuncionProceso")
references "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::CRE.ModuloRequisitoAperturaCierre"
add constraint "FK_Estado_ModuloRequisitoAperturaCierre" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: "BSCL.Plataforma.BD::PLA.CRE.ModuloFuncionProcesoDependencia" */
alter table "BSCL.Plataforma.BD::CRE.ModuloFuncionProcesoDependencia"
add constraint "FK_ModuloFuncionProcesoPrincipal_ModuloFuncionProcesoDependencia" foreign key ("IdModuloFuncionProcesoPrincipal")
references "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::CRE.ModuloFuncionProcesoDependencia"
add constraint "FK_ModuloFuncionProceso_ModuloFuncionProcesoDependencia" foreign key ("IdModuloFuncionProceso")
references "BSCL.Plataforma.BD::CRE.ModuloFuncionProceso" ("IdModuloFuncionProceso") on delete restrict on update restrict;

alter table "BSCL.Plataforma.BD::CRE.ModuloFuncionProcesoDependencia"
add constraint "FK_Estado_ModuloFuncionProcesoDependencia" foreign key ("IdEstado")
references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* secuencia BSCL.Plataforma.BD.Secuencias::CREModuloRequisitoAperturaCierre */
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::CRE.ModuloRequisitoAperturaCierre" ;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::CRE.ModuloRequisitoAperturaCierre" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   