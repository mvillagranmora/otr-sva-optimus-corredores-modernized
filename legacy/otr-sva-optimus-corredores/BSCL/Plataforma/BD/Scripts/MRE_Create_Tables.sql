SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.Plataforma.BD::MRE.Regla"; 
DROP TABLE "BSCL.Plataforma.BD::MRE.ReglaEjecucion"; 
DROP TABLE "BSCL.Plataforma.BD::MRE.ReglaGrupo"; 
DROP TABLE "BSCL.Plataforma.BD::MRE.ReglaOperacion"; 
DROP TABLE "BSCL.Plataforma.BD::MRE.ReglaSalida"; 



/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.Plataforma.BD::MRE.Regla" (
	 "CodigoRegla" 	NVARCHAR(40)  NOT NULL ,
	 "Modulo" 		NVARCHAR(100) NOT NULL ,
	 "Descripcion" 	NVARCHAR(520) NOT NULL ,
	 "EsquemaDB" 	NVARCHAR(520) NOT NULL ,
	 "FuncionSP" 	NVARCHAR(260) NOT NULL ,
	 "Parametros" 	NVARCHAR(260) DEFAULT ' ' NOT NULL , 
	 PRIMARY KEY ("CodigoRegla")
 );


CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::MRE.ReglaEjecucion"  ( 
	 "Id" 			BIGINT 			CS_FIXED  NOT NULL,
	 "Resultado" 	NVARCHAR  	 	CS_STRING NOT NULL,
	 "Mensaje" 		NVARCHAR(520)  	CS_STRING NOT NULL, 
	 PRIMARY KEY ( "Id" ) 
 );

CREATE COLUMN TABLE "BSCL.Plataforma.BD::MRE.ReglaGrupo" (
	 "IdGrupoRegla" 			BIGINT CS_FIXED 	 NOT NULL ,
	 "IdEmpresa" 				BIGINT CS_FIXED 	 NOT NULL ,
	 "Modulo" 					NVARCHAR(100) 		 NOT NULL ,
	 "Descripcion" 				NVARCHAR(520) 		 NOT NULL ,
	 "ProcedimientoCompilado" 	NVARCHAR(520) 		 DEFAULT ' ' NOT NULL ,
	 "FechaCompilado" 			LONGDATE CS_LONGDATE DEFAULT '0001-01-01' NOT NULL , 
	 PRIMARY KEY ("IdGrupoRegla")
 );

CREATE COLUMN TABLE "BSCL.Plataforma.BD::MRE.ReglaOperacion" (
	 "IdReglaOperacion" 	BIGINT CS_FIXED 	NOT NULL,
	 "IdEmpresa" 			BIGINT CS_FIXED 	NOT NULL,
	 "IdGrupoRegla" 		BIGINT CS_FIXED 	NOT NULL DEFAULT 0,
	 "Prioridad" 			INTEGER CS_INT 		NOT NULL,
	 "DetieneValidacion" 	NVARCHAR(1) 		NOT NULL,
	 "CodigoRegla" 			NVARCHAR(40) 		NOT NULL,
	 "Ambito" 				NVARCHAR(40) 		NOT NULL,
	 "Habilitado" 			NVARCHAR(1) 		NOT NULL,
	 "CodigoComportamiento" NVARCHAR(40) 		NOT NULL, 
	 PRIMARY KEY ("IdReglaOperacion")
 );


CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::MRE.ReglaSalida"  ( 
	 "IdEjecucion" 			BIGINT CS_FIXED 		 NOT NULL,
	 "IdSecuencia" 			BIGINT CS_FIXED 		 NOT NULL,
	 "idRegla" 				BIGINT CS_FIXED,
	 "Habilitado" 			NVARCHAR  CS_STRING 	 NOT NULL,
	 "CodComportamiento" 	NVARCHAR(40)  CS_STRING  NOT NULL,
	 "ResultadoEvaluacion" 	INT CS_INT 				 NOT NULL,
	 "ReglaActivada" 		INT CS_INT 				 NOT NULL,
	 "MensajeRegla" 		NVARCHAR(520)  CS_STRING NOT NULL,
	  PRIMARY KEY ( "IdEjecucion", "IdSecuencia" ) 
  );


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */



/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */

	
	
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit




