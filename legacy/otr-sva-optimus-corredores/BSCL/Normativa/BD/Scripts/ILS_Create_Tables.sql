SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
 
DROP TABLE  "BSCL"."BSCL.Normativa.BD::ILS.Indice"   CASCADE;
DROP TABLE  "BSCL"."BSCL.Normativa.BD::ILS.IndiceConfiguracion";
DROP TABLE  "BSCL"."BSCL.Normativa.BD::ILS.InformeTipo"   CASCADE;
DROP TABLE  "BSCL"."BSCL.Normativa.BD::ILS.InformeResultado";
DROP TABLE  "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoIndice";
DROP TABLE  "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoConcepto";
DROP TABLE  "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoItem";
DROP TABLE  "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoItemDetalle";
DROP TABLE  "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoItemDig";


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/


CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.Indice" 
	(
	 "IdIndice" BIGINT CS_FIXED NOT NULL ,
	 "IdEmpresa" BIGINT CS_FIXED NOT NULL ,
	 "Nombre" NVARCHAR(200) DEFAULT '' NOT NULL ,
	 "NombreCorto" NVARCHAR(20) DEFAULT '' NOT NULL ,
	 "Estado" NVARCHAR(3) DEFAULT '' NOT NULL ,
	 PRIMARY KEY ("IdIndice")) UNLOAD PRIORITY 5 AUTO MERGE;
 

CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.IndiceConfiguracion" 
	(
	 "IdIndice" BIGINT NOT NULL ,
	 "IdConcepto" BIGINT  NOT NULL ,
	 "IdEmpresa" BIGINT NOT NULL ,
	 "Posicion" NVARCHAR(1)  NOT NULL ,
	 "Factor" DECIMAL(8,6)  NOT NULL ,
	 "Operacion" NVARCHAR(1)  NOT NULL ,	
	 "Estado" NVARCHAR(3) 	NOT NULL,
     PRIMARY KEY ("IdIndice","IdConcepto"));
 

CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.InformeTipo" 
	(
	 "IdTipoInforme" BIGINT 		NOT NULL,
	 "IdEmpresa" 	 BIGINT 		NOT NULL,
	 "Nombre" 	     NVARCHAR(200) 	NOT NULL,		
	 "Estado" 	     NVARCHAR(3) 	NOT NULL,
	 PRIMARY KEY ("IdTipoInforme"));	
 
CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.InformeResultado" 
	(
	 "IdTipoInforme"     BIGINT 		NOT NULL,
	 "FechaContable" 	 NVARCHAR(10) 	NOT NULL,
	 "FechaEmision"      NVARCHAR(10)	NOT NULL,
	 "HoraEmision" 	     NVARCHAR(8) 	NOT NULL,
	 "Estado" 	         NVARCHAR(3) 	NOT NULL,
	 PRIMARY KEY ("IdTipoInforme","FechaContable"));	
 								    					

CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoIndice" 
	(
	 "IdResultadoIndice" BIGINT 		NOT NULL,
	 "IdTipoInforme"     BIGINT 		NOT NULL,
	 "FechaContable" 	 NVARCHAR(10)	NOT NULL,
	 "Monto" 	         DECIMAL(20,4)	NOT NULL,
	 "IdIndice" 	     BIGINT 		NOT NULL,
	 "NombreIndice" 	 NVARCHAR(200)	NOT NULL,
	 "Estado" 	         NVARCHAR(3) 	NOT NULL,
	 PRIMARY KEY ("IdResultadoIndice"));	
	 
CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoConcepto" 
	(
	 "IdResultadoConcepto" BIGINT 		NOT NULL,
	 "IdResultadoIndice"   BIGINT 		NOT NULL,
	 "Monto" 	           DECIMAL(20,4)	NOT NULL,
	 "NombreConcepto" 	   NVARCHAR(200)	NOT NULL,
	 "IdConcepto"          BIGINT 		NOT NULL,
	 "Posicion" 	       NVARCHAR(1)	NOT NULL,
	 "Operador" 	       NVARCHAR(1)	NOT NULL,
	 "Factor" 	           DECIMAL(20,4)	NOT NULL,
	 "MontoCalculado"     DECIMAL(20,4) 		NOT NULL,
	 "Estado" 	           NVARCHAR(3) 	NOT NULL,
	 PRIMARY KEY ("IdResultadoConcepto"));	
	 
CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoItem" 
	(
	 "IdResultadoItem"   BIGINT 		NOT NULL,
	 "IdResultadoConcepto" BIGINT 		NOT NULL,
	 "IdTipoItem" 	       NVARCHAR(3)	NOT NULL,
	 "IdItem" 	   BIGINT	NOT NULL,
	 "NombreItem"          NVARCHAR(200)		NOT NULL,
	 "Monto" 	       DECIMAL(20,4)	NOT NULL,
	 "Operacion" 	       NVARCHAR(1)	NOT NULL,
	 "Factor" 	           DECIMAL(20,4)	NOT NULL,
	 "MontoCalculado"     DECIMAL(20,4) 		NOT NULL,
	 "Posicion" 	           NVARCHAR(3) 	NOT NULL,
	 PRIMARY KEY ("IdResultadoItem"));	
	 
CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoItemDetalle" 
	(
	 "IdResultadoItemDetalle"   BIGINT 			NOT NULL,
	 "IdResultadoItem"   		BIGINT 			NOT NULL,
	 "IdTipoItem" 	       		NVARCHAR(3)		NOT NULL,
	 "Monto" 	       			DECIMAL(20,4)	NOT NULL,
	 "IdItem" 	   				BIGINT			NOT NULL,
	 "NombreItem"          		NVARCHAR(200)	NOT NULL,
	 "CuentaContable" 	       	NVARCHAR(12)	NOT NULL,
	 "SaldoDebeHaber" 	       	NVARCHAR(1)		NOT NULL,
	 "Condicion" 	       		NVARCHAR(1)		NOT NULL,
	 "MontoCondicion" 	   		DECIMAL(20,4)	NOT NULL,
	 "Operacion" 	       		NVARCHAR(1)		NOT NULL,
	 "Factor" 	           		DECIMAL(20,4)	NOT NULL,
	 "MontoCalculado"     		DECIMAL(20,4) 	NOT NULL,
	 "Posicion" 	           NVARCHAR(1) 		NOT NULL,
	 "MayorACero" 	           NVARCHAR(1) 		NOT NULL,
	 "MenorACero" 	           NVARCHAR(1) 		NOT NULL,
	 "PermiteCero" 	           NVARCHAR(1) 		NOT NULL,
	 "ProcesoFuncion" 	       NVARCHAR(200) 	NOT NULL,
	 PRIMARY KEY ("IdResultadoItemDetalle"));	


CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.InformeResultadoItemDig" (
       "IdTipoInforme" 		BIGINT 			NOT NULL, 
       "NombreInforme" 		NVARCHAR(200) 	NOT NULL, 
       "OrdenInforme" 		INT 			NOT NULL, 
       "IdEmpresa" 			INT 			NOT NULL, 
       "FechaContable" 		NVARCHAR(10) 	NOT NULL, 
   	   "IdIndice" 			INT 			NOT NULL, 
       "NombreIndice" 		NVARCHAR(200) 	NOT NULL,
       "PosicionIndice" 	NVARCHAR(1) 	NOT NULL, 
       "OperadorIndice" 	INT 			NOT NULL, 
       "FactorIndice" 		DECIMAL(5, 3) 	NOT NULL, 
       "IdConcepto" 		INT 			NOT NULL, 
       "NombreConcepto" 	NVARCHAR(200) 	NOT NULL, 
   	   "OperacionConcepto" 	INT 			NOT NULL, 
       "FactorConcepto" 	DECIMAL(5, 3) 	NOT NULL, 
       "IdItem" 			INT 			NOT NULL, 
       "NombreItem" 		NVARCHAR(200) 	NOT NULL, 
       "IdTipoItem" 		NVARCHAR(3) 	NOT NULL, 
       "MayorACeroItem" 	NVARCHAR(1) 	NOT NULL, 
       "MenorACeroItem" 	NVARCHAR(1) 	NOT NULL, 
       "PermiteCeroItem" 	NVARCHAR(1) 	NOT NULL, 
       "Monto" 				DECIMAL(20, 4) 	NOT NULL,
       CONSTRAINT "PK_ILS_InformeResultadoItemDig" 
       PRIMARY KEY INVERTED VALUE ("IdTipoInforme", "IdEmpresa", "FechaContable", "IdIndice", "OperadorIndice", "FactorIndice", "IdConcepto", "OperacionConcepto", "FactorConcepto", "IdItem", "PosicionIndice"));
       
       
       CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::ILS.InformeConfiguracion" (
       "IdTipoInforme" 		BIGINT 	CS_FIXED 	NOT NULL,
       "IdIndice" 			INTEGER CS_INT 		NOT NULL,
       "Orden" 				INTEGER CS_INT 		NOT NULL,
       CONSTRAINT "PK_ILS_InformeConfiguracion" 
       PRIMARY KEY INVERTED VALUE ("IdTipoInforme", "IdIndice"));
	 			     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/


DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdIndice"	CASCADE;
DROP SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdInformeTipo"	CASCADE;

CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdIndice"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
CREATE SEQUENCE "BSCL.Normativa.BD.Secuencias::ILS.IdInformeTipo"
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
       
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit






