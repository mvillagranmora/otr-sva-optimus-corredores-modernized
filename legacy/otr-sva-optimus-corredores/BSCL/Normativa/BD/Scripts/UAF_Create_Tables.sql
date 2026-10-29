SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/


DROP TABLE  "BSCL.Normativa.BD::UAF.MedioDePagoDOF"  CASCADE;
DROP TABLE  "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI"  CASCADE;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/




CREATE COLUMN TABLE "BSCL.Normativa.BD::UAF.MedioDePagoDOF" (
   "IdEmpresa" 					BIGINT CS_FIXED NOT NULL ,
   "IdMedioDePago" 				NVARCHAR(3) NOT NULL,
   "IdEstado" 				 	NVARCHAR(3) DEFAULT 'ACT' NOT NULL,
   PRIMARY KEY ("IdEmpresa","IdMedioDePago")
);

 
 
 CREATE COLUMN TABLE "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI" (
   "IdEvaluacion"					BIGINT			NOT NULL ,
   "IdEmpresa" 					    BIGINT CS_FIXED NOT NULL ,
   "IdModulo"						NVARCHAR(3)		NOT NULL ,
   "IdCliente"					    BIGINT			NOT NULL ,
   "IdPortafolio"					BIGINT			NOT NULL ,
   "IdTipoProducto" 				NVARCHAR(4)		NOT NULL DEFAULT '',
   "IdTipoOperacion"    			NVARCHAR(6)		NOT NULL ,
   "IdOperacion" 					BIGINT			NOT NULL ,
   "MontoUtilizado"          		DECIMAL(26,8) 	NOT NULL DEFAULT '0',
   "IdMoneda"                		NVARCHAR(3)		NOT NULL DEFAULT '',
   "IdTipoInstrumento"       		NVARCHAR(3)		NOT NULL DEFAULT '',
   "IdClasificacionTipoInstrumento" NVARCHAR(3)		NOT NULL DEFAULT '',
   "FechaOperacion"					NVARCHAR(10)	NOT NULL DEFAULT '',--'YYYY-MM-DD'
    ---Evaluacion
	"TipoEvaluacion" 				NVARCHAR(40)	NOT NULL DEFAULT '',
	"EstMontoTotalDias"        		DECIMAL(26,8) 	NOT NULL DEFAULT '0',
    "EstCantidadDias"        		BIGINT 			NOT NULL DEFAULT '0',
    "EstMontoPromedioDiario"   		DECIMAL(26,8) 	NOT NULL DEFAULT '0',
    "EstMontoMaximo"		   		DECIMAL(26,8) 	NOT NULL DEFAULT '0',
    "Patrimonio"        			DECIMAL(26,8) 	NOT NULL DEFAULT '0',
    ---Calculos
	"ConceptoEvaluacion" 			NVARCHAR(40)	NOT NULL DEFAULT '',
	"CalDiferencia"   				DECIMAL(26,8) 	NOT NULL DEFAULT '0',
	"CalTolerancia"					DECIMAL(26,8) 	NOT NULL DEFAULT '0',
	"Factor"						DECIMAL(26,8) 	NOT NULL DEFAULT '0',
	---Auditoria
   "FechaIngreso"       			SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "FechaCreacion"     				SECONDDATE		DEFAULT '0001-01-01 00:00:00' NOT NULL,
   "IdUsuarioCreador"   			BIGINT			DEFAULT 0,
   PRIMARY KEY ("IdEvaluacion")
--   PRIMARY KEY ("IdEmpresa","IdModulo","IdCliente","IdPortafolio","IdOperacion","IdTipoOperacion","TipoEvaluacion")
);
 
 
 
									    								     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/


DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::UAF.IdEvaluacion"	CASCADE;

CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::UAF.IdEvaluacion"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit






