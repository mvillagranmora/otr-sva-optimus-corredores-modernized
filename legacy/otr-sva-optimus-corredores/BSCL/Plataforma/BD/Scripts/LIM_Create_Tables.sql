SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Plataforma.BD::LIM.EstadoLim";
drop table "BSCL.Plataforma.BD::LIM.LimiteEmisor";
--drop table "BSCL.Plataforma.BD::LIM.LimiteClasificacionRiesgo";
drop table "BSCL.Plataforma.BD::LIM.GrupoLimite";
drop table "BSCL.Plataforma.BD::LIM.TipoLimite";
drop table "BSCL.Plataforma.BD::LIM.PlantillaLimite";
drop table "BSCL.Plataforma.BD::LIM.Limite";
drop table "BSCL.Plataforma.BD::LIM.RangosMontoPlazo";
drop table "BSCL.Plataforma.BD::LIM.EvaluacionLimites";
drop table "BSCL.Plataforma.BD::LIM.ExcluirLimite";
drop table "BSCL.Plataforma.BD::LIM.Configuracion";
drop table "BSCL.Plataforma.BD::LIM.GrupoEmisor";
drop table "BSCL.Plataforma.BD::LIM.GrupoEmisorDetalle";


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.Plataforma.BD::LIM.EstadoLim" (
	"IdEstadoLim"	BIGINT			NOT NULL,
	"IdEstado"		NVARCHAR(3)		NOT NULL,
	"NombreEntidad"	NVARCHAR(100)	NOT NULL,
	"Descripcion"	NVARCHAR(520)	NOT NULL,
	"Prioridad"		BIGINT			NOT NULL DEFAULT 0, --Prioridad  del estado
	PRIMARY KEY ("IdEstadoLim")
);
 
create column table "BSCL.Plataforma.BD::LIM.LimiteEmisor" (
   "IdLimiteEmisor"			BIGINT			NOT NULL,
   "IdEmisor"				NVARCHAR(40)	NOT NULL DEFAULT '',
   "IdLimite"				BIGINT			NOT NULL,
   "IdEstado"				NVARCHAR(3)		NOT NULL DEFAULT '', 
   "FechaIngreso"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaCreacion"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaModificacion"		SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "IdEstadoAutorizacion"	NVARCHAR(3)		DEFAULT 'PEN' NOT NULL,
   "Borrado"				NVARCHAR(1)		DEFAULT 'N',
   "IdUsuarioCreador"		BIGINT			DEFAULT 0,   
	primary key ("IdLimiteEmisor")
);

/*
create column table "BSCL.Plataforma.BD::LIM.LimiteClasificacionRiesgo" (
   "IdLimiteClasificacionRiesgo" BIGINT NOT NULL, 
   "IdClasificacionRiesgo"       BIGINT NOT NULL DEFAULT '0',
   "IdLimite"                    BIGINT NOT NULL,
   "IdEstado"                    NVARCHAR(3) NOT NULL DEFAULT '',       
primary key ("IdLimiteClasificacionRiesgo")
   );
*/   

/*
Maneja el agrupamiento lógico de los limites en las aplicaciones
- RV Limites de Renta Variable
- RF Limites de Renta Fija
- SPOT Limites de Mesa Spot
- CLIENTE
- AGENTE
*/
create column table "BSCL.Plataforma.BD::LIM.GrupoLimite" (
	"IdGrupoLimite"	NVARCHAR(40)  NOT NULL,
	"Descripcion"	NVARCHAR(100) NOT NULL,
	primary key ("IdGrupoLimite")
);

create column table "BSCL.Plataforma.BD::LIM.TipoLimite" (
	"IdTipoLimite"		NVARCHAR(40)	NOT NULL,
	"IdEmpresa"			BIGINT			NOT NULL,
	"Nombre"			NVARCHAR(100)	NOT NULL,
	"EsCuentaPropia"	NVARCHAR(1)		NOT NULL DEFAULT 'S',
	"IdComportamiento"	NVARCHAR(3)		NOT NULL DEFAULT '', --Requiere autorizacion, Advertencia, Warning
	"IdGrupoLimite"  	NVARCHAR(40)	NOT NULL DEFAULT '', -- Clasificacion (Mercado) RV, MO, RF
	"Categoria"        	NVARCHAR(40)	NOT NULL DEFAULT ' ', -- de POSICION O CARTERA, de OPERACION, de MONTO OPERADO
	"IdEstado"         	NVARCHAR(3)		NOT NULL DEFAULT '',
	"IdTipoEvento"		NVARCHAR(6)		NOT NULL DEFAULT '',--Tipo evento de autorizacion
	"Prioridad"			BIGINT			NOT NULL DEFAULT 0, --Prioridad de autorizacion
	primary key ("IdTipoLimite")
);

   
create column table "BSCL.Plataforma.BD::LIM.PlantillaLimite" (
	"IdPlantillaLimite"				NVARCHAR(40)	NOT NULL,
	"DescripcionPlantillaLimite"	NVARCHAR(100)	NOT NULL,
	"IdCategoriaInversionista"		NVARCHAR(40)	NOT NULL,
	"EsPerfilPersonalizado"			NVARCHAR(1)		DEFAULT '' NOT NULL,
	"FechaCreacion"					DATE			NOT NULL,
	"FechaModificacion"				DATE			NOT NULL,
	"FechaIngreso" 					SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01 00:00:00' NOT NULL ,
	"IdEstadoAutorizacion"			NVARCHAR(3)		DEFAULT '' NOT NULL ,--*
	"IdUsuarioCreador"  			BIGINT			DEFAULT 0,
	"IdEstado"					   	NVARCHAR(3)		NOT NULL,
	"PorDefecto"					NVARCHAR(1)		DEFAULT 'N' NOT NULL,
	"IdEmpresa"						BIGINT			NOT NULL,
	primary key ("IdPlantillaLimite")
);
   
   
create column table "BSCL.Plataforma.BD::LIM.Limite" (
	"IdLimite"                  BIGINT			NOT NULL,
    "IdPlantillaLimite"			NVARCHAR(40)	DEFAULT '' NOT NULL,
	"IdTipoLimite"              NVARCHAR(40)	NOT NULL,		  --- 'LIMRVCPCARTERA'	
	"Nombre"               		NVARCHAR(100)	NOT NULL,
	"IdMesa"                    BIGINT			NOT NULL DEFAULT '0', 
	"IdCanalIngreso" 			NVARCHAR(3)		NOT NULL, -- CANAL AGENTE, CANAL ICLIENTE
	"IdCategoriaInversionista"	NVARCHAR(40)	NOT NULL DEFAULT '',
	--"IdUsuario"  				BIGINT			NOT NULL DEFAULT '0', --Agentes Especificos
	--"IdCliente" 				BIGINT			NOT NULL DEFAULT '0', --cliente
	--"IdPortafolio" 				BIGINT NOT NULL, --Portafolio
	"IdTipoInstrumento"         NVARCHAR(3)		NOT NULL DEFAULT '', --'ACN','SIC'
	"IdTipoOperacion"           NVARCHAR(6)		NOT NULL DEFAULT '',
    --"IdTipoProducto"			NVARCHAR(4)		NOT NULL DEFAULT '', 
    "IdInstrumento"          	BIGINT,
	"UnidadPlazo"               NVARCHAR(1)		NOT NULL DEFAULT 'D',
	"Plazo"                     INTEGER			NOT NULL DEFAULT '0',
	"IdTipoReajuste"            NVARCHAR(3)		NOT NULL DEFAULT '',
	"CodigoOperador"			NVARCHAR(40)	NOT NULL DEFAULT '',
	"MontoTotal"                DECIMAL(26,8)	NOT NULL DEFAULT '0', --LIMITE TOTAL
	"MontoIntraday"             DECIMAL(26,8)	NOT NULL DEFAULT '0',
    "MontoOvernight"            DECIMAL(26,8)	NOT NULL DEFAULT '0',
	"IdMoneda"                  NVARCHAR(3)		NOT NULL DEFAULT '',
    "IdMonedaCalculo"           NVARCHAR(3)		NOT NULL DEFAULT '',
    "ClasificacionRiesgoMinima" NVARCHAR(20)	NOT NULL DEFAULT '',
    "IdEstado"                  NVARCHAR(3)		NOT NULL DEFAULT '',
    "EsPersonalizada"			NVARCHAR(1)		NOT NULL DEFAULT 'N', -- S o N
	"FechaVigencia"				DATE			NOT NULL DEFAULT '0001-01-01',
	"EsTotalizador"		        NVARCHAR(1)		NOT NULL DEFAULT 'N', -- S o N
	"FechaIngreso"       		SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
    "FechaCreacion"     		SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
    "FechaModificacion"  		SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"IdEstadoAutorizacion" 		NVARCHAR(3)		DEFAULT 'PEN' NOT NULL,
	"Borrado" 					NVARCHAR(1)		DEFAULT 'N',
    "IdUsuarioCreador"   		BIGINT			DEFAULT 0,
    "UI_Limite"           		NVARCHAR(16)	NOT NULL DEFAULT '',
    "IdClasificacionRiesgo"		BIGINT			NOT NULL DEFAULT '0',
    "IdAgente"  				BIGINT			NOT NULL DEFAULT '0', --guarda IdEmpleado
	"IdClasificacionTipoInstrumento" NVARCHAR(3) NOT NULL DEFAULT '',
	"Porcentaje"				DECIMAL(26.8)	NOT NULL DEFAULT '0',
	"IdGrupoEmisor"				NVARCHAR(40) 	NOT NULL DEFAULT '',
	"IdEmisor"					NVARCHAR(40) 	NOT NULL DEFAULT '',
	primary key ("IdLimite")
);


create column table "BSCL.Plataforma.BD::LIM.RangosMontoPlazo" (
	"IdRangosMontoPlazo"	BIGINT			NOT NULL,
	"IdLimite"				BIGINT			NOT NULL,
	"MontoDesde"			DECIMAL(26,8)	NOT NULL DEFAULT '0',
	"MontoHasta"			DECIMAL(26,8)	NOT NULL DEFAULT '0',	
	"PlazoDesde"			INTEGER			NOT NULL DEFAULT '0',
    "PlazoHasta"			INTEGER			NOT NULL DEFAULT '0',
	"Tasa"					DECIMAL(26,8)	NOT NULL DEFAULT '0',
    "PuntosTasaMenos"		DECIMAL(26,8)	NOT NULL DEFAULT '0',
    "PuntosTasaMas"			DECIMAL(26,8)	NOT NULL DEFAULT '0',
    "PuntosTasaMenosAgente"	DECIMAL(26,8)	NOT NULL DEFAULT '0',
    "PuntosTasaMasAgente"	DECIMAL(26,8)	NOT NULL DEFAULT '0',
    "IdEstado"				NVARCHAR(3)		NOT NULL DEFAULT '', 
    "FechaIngreso"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
    "FechaCreacion"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
    "FechaModificacion"		SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
    "IdEstadoAutorizacion"	NVARCHAR(3)		DEFAULT 'PEN' NOT NULL,
    "Borrado"				NVARCHAR(1)		DEFAULT 'N',
    "IdUsuarioCreador"		BIGINT			DEFAULT 0,
    primary key ("IdRangosMontoPlazo")
);	

create column table "BSCL.Plataforma.BD::LIM.EvaluacionLimites" (
	"IdEvaluacionLimite"	BIGINT			NOT NULL,
	"IdSecuenciaEvaluacion"	BIGINT			NOT NULL,
	"IdLimite"				BIGINT			NOT NULL,
	"IdTipoOperacion"		NVARCHAR(6)		NOT NULL,
	"IdOperacion"			BIGINT			NOT NULL DEFAULT 0,
	"IdUsuarioAutoriza"		BIGINT			NOT NULL,
	"IdMoneda"				NVARCHAR(3)		NOT NULL,
	"ConceptoExceso"		NVARCHAR(520)	NOT NULL,
	"MontoExcedido"			DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"MontoUtilizado"		DECIMAL(26,8)	NOT NULL DEFAULT 0,
	"FechaIngreso"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"FechaCreacion"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"IdEstado"				NVARCHAR(3)		NOT NULL DEFAULT '',
	"FechaAutorizacion"		SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"IdEstadoAutorizacion"	NVARCHAR(3)		DEFAULT 'PEN' NOT NULL,
	"IdUsuarioCreador"		BIGINT			DEFAULT 0,
	"IdTipoEvento"			NVARCHAR(6)		NOT NULL DEFAULT '',--Tipo evento de autorizacion
	"Prioridad"				BIGINT			NOT NULL DEFAULT 0, --Prioridad de autorizacion
	"ConceptoEvaluacion"	NVARCHAR(40)	NOT NULL DEFAULT '',--Concepto Evaluado: Monto, tasa,MontoIntraday etc.
	"IdTipoEntidad"         NVARCHAR(40)    NOT NULL DEFAULT '',
	"IdTipoInstrumento"     NVARCHAR(3)		NOT NULL DEFAULT '',
	"IdClasificacionTipoInstrumento" NVARCHAR(3) NOT NULL DEFAULT '',
	"IdTipoLimite"          NVARCHAR(40)	NOT NULL  DEFAULT '',
	"MontoLimite"           DECIMAL(26,8)	NOT NULL DEFAULT '0',
	"MontoCalculado"        DECIMAL(26,8)	NOT NULL DEFAULT '0',
	"IdMonedaCalculo"       NVARCHAR(3)		NOT NULL DEFAULT '',
	"IdCliente" 			BIGINT			NOT NULL DEFAULT '0', 
	"IdPortafolio" 			BIGINT 			NOT NULL DEFAULT '0',
	"IdCanalIngreso" 		NVARCHAR(3) 	NOT NULL DEFAULT '',
	primary key ("IdEvaluacionLimite","IdSecuenciaEvaluacion")
);



--Excluir Limite
create column table "BSCL.Plataforma.BD::LIM.ExcluirLimite" (
	"IdModulo"			NVARCHAR(3)		NOT NULL,
	"IdTipoInstrumento"	NVARCHAR(3)		DEFAULT '',
	"IdEstado"			NVARCHAR(3)		NOT NULL DEFAULT 'ACT',
	"TipoExclusion"		NVARCHAR(40)	NOT NULL,
	"IdEmpresa"			BIGINT			NOT NULL,
	primary key ("IdModulo","IdTipoInstrumento","TipoExclusion")
);


--Parametros de Configuracion de los Limites
CREATE COLUMN TABLE "BSCL"."BSCL.Plataforma.BD::LIM.Configuracion" (
	"IdEmpresa"			BIGINT CS_FIXED NOT NULL,
	"Codigo"			NVARCHAR(40) 	NOT NULL,
	"Descripcion"		NVARCHAR(520) 	NOT NULL,
	"Valor"				NVARCHAR(520) 	NOT NULL,
	PRIMARY KEY ("IdEmpresa", "Codigo")
);


--GrupoEmisor
create column table "BSCL.Plataforma.BD::LIM.GrupoEmisor" (
	"IdGrupoEmisor"			NVARCHAR(40) 	NOT NULL,
	"IdEmpresa"				BIGINT			NOT NULL,
	"Descripcion"			NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdEstado"				NVARCHAR(3)		NOT NULL DEFAULT '',
	"FechaIngreso"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"FechaCreacion"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"FechaModificacion"		SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"IdEstadoAutorizacion"	NVARCHAR(3)		DEFAULT 'ING' NOT NULL,
	"Borrado"				NVARCHAR(1)		DEFAULT 'N',
	"IdUsuarioCreador"		BIGINT			DEFAULT 0,
	primary key ("IdGrupoEmisor")
);


--GrupoEmisorDetalle
create column table "BSCL.Plataforma.BD::LIM.GrupoEmisorDetalle" (
	"IdGrupoEmisorDetalle"	BIGINT			NOT NULL,
	"IdGrupoEmisor"			NVARCHAR(40) 	NOT NULL ,
	"IdEmisor"				NVARCHAR(40)	NOT NULL DEFAULT '',
	"IdEstado"				NVARCHAR(3)		NOT NULL DEFAULT '', 
	"FechaIngreso"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"FechaCreacion"			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"FechaModificacion"		SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
	"IdEstadoAutorizacion"	NVARCHAR(3)		DEFAULT 'PEN' NOT NULL,
	"Borrado"				NVARCHAR(1)		DEFAULT 'N',
	"IdUsuarioCreador"		BIGINT			DEFAULT 0,   
	primary key ("IdGrupoEmisorDetalle")
);



/*==============================================================*/
/* Secuences:                                                  */
/*==============================================================*/
--drop sequence "BSCL.Plataforma.BD.Secuencias::Limite";
--create sequence "BSCL.Plataforma.BD.Secuencias::Limite" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;

DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdLimite"					CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdEvaluacionLimite"		CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdSecuenciaEvaluacion"	CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdLimiteEmisor"			CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdRangosMontoPlazo"		CASCADE;
DROP SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdGrupoEmisorDetalle"		CASCADE;


CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdLimite"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdEvaluacionLimite"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdSecuenciaEvaluacion"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdLimiteEmisor"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;

CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdRangosMontoPlazo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::LIM.IdGrupoEmisorDetalle"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
/*		
create column table "BSCL.Plataforma.BD::LIM.Limite" (
	"IdLimite"                   BIGINT NOT NULL,
    "IdTipoLimite"               BIGINT NOT NULL,                   
    "IdTipoProducto"             NVARCHAR(4) NOT NULL DEFAULT '',
    "IdTipoOperacion"            NVARCHAR(6) NOT NULL DEFAULT '',???????
    "IdTipoInstrumento"          NVARCHAR(3) NOT NULL DEFAULT '',
    "Nemo"					     NVARCHAR(40) NOT NULL DEFAULT '',
    "IdInstrumento"              BIGINT NOT NULL DEFAULT '0',????????
    "IdTipoClasificacionRiesgo"  NVARCHAR(40) NOT NULL DEFAULT '',??????????
    "IdClasificacionRiesgo"      BIGINT NOT NULL DEFAULT '0',?????
    "IdEmisor"                   NVARCHAR(40) NOT NULL DEFAULT '',???????
    "IdMoneda"                   NVARCHAR(3) NOT NULL DEFAULT '',
    "IdMonedaCalculo"            NVARCHAR(3) NOT NULL DEFAULT '',
    "IdTipoReajuste"             NVARCHAR(3) NOT NULL DEFAULT '',
    "IdCliente"                  BIGINT NOT NULL DEFAULT '0',??????
    "IdPortafolio"               BIGINT NOT NULL DEFAULT '0',???????
    "IdRolUsuario"               BIGINT NOT NULL DEFAULT '0',??????
    "IdPerfilInversionista"      BIGINT NOT NULL DEFAULT '0',????
    "IdTipoMesa"                 BIGINT NOT NULL DEFAULT '0',????
    "IdMesa"                     BIGINT NOT NULL DEFAULT '0',
    "IdCanal"				     NVARCHAR(3) NOT NULL DEFAULT '',
    "MontoDesde"                 DECIMAL (26,8) NOT NULL DEFAULT '0',
    "MontoHasta"                 DECIMAL (26,8) NOT NULL DEFAULT '0',
    "PlazoDesde"                 INTEGER NOT NULL DEFAULT '0',
    "PlazoHasta"                 INTEGER NOT NULL DEFAULT '0',
    "UnidadPlazo"                NVARCHAR(1) NOT NULL DEFAULT 'D',
    "TasaDesde"                  DECIMAL (26,8) NOT NULL DEFAULT '0',??????? NO ES SOLO TASA
    "TasaHasta"                  DECIMAL (26,8) NOT NULL DEFAULT '0',??????? NO ES SOLO TASA
    "PuntosTasaMenos"            INTEGER NOT NULL DEFAULT '0',
    "PuntosTasaMas"              INTEGER NOT NULL DEFAULT '0',
    "PuntosTasaMenosAgente"      INTEGER NOT NULL DEFAULT '0',
    "PuntosTasaMasAgente"        INTEGER NOT NULL DEFAULT '0',
    "MontoIntraday"              DECIMAL (26,8) NOT NULL DEFAULT '0',
    "MontoOvernigth"             DECIMAL (26,8) NOT NULL DEFAULT '0',
    "OperaContraCtaInversion"    NVARCHAR(1) NOT NULL DEFAULT 'N', ??????
    "PermiteSobregiro"           NVARCHAR(1) NOT NULL DEFAULT 'N', ??????
    "MontoSobregiro"             DECIMAL (26,8) NOT NULL DEFAULT '0',
    "IdEstado"                   BIGINT NOT NULL,                 
	primary key ("IdLimite")
);	
*/

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */
/*alter table "BSCL.Plataforma.BD::LIM.Limite"
       alter ("IdTipoReajuste" NVARCHAR(3) NOT NULL DEFAULT '');*/

/*alter table "BSCL.Plataforma.BD::LIM.Limite"
   add constraint "FK_TipoLimite_Limite" foreign key ("IdTipoLimite")
      references "BSCL.Plataforma.BD::LIM.TipoLimite" ("IdTipoLimite");
*/

/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */
--create index "INX01_" on "BSCL.Plataforma.BD::LIM."("");


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit
