SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables: BSCL.Mesa.BD::SPT                              */
/*==============================================================*/
DROP TABLE  "BSCL.Normativa.BD::ILS.Item";
DROP TABLE  "BSCL.Normativa.BD::ILS.Concepto";
DROP TABLE  "BSCL.Normativa.BD::ILS.ConceptoConfiguracion";
DROP TABLE  "BSCL.Normativa.BD::ILS.ItemTipo";
DROP TABLE  "BSCL.Normativa.BD::ILS.ProcesoFuncion";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.Item"
( "IdItem"				BIGINT			DEFAULT '0'	NOT NULL
, "IdTipoItem"			NVARCHAR(3)		DEFAULT ''	NOT NULL
, "Nombre" 				NVARCHAR(200)	DEFAULT ''	NOT NULL
, "NombreCorto"			NVARCHAR(20)	DEFAULT ''	NOT NULL
, "Estado"				NVARCHAR(3) 	DEFAULT ''	NOT NULL
,PRIMARY KEY ("IdItem")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.Concepto"
( "IdConcepto"				BIGINT		DEFAULT '0'	NOT NULL
, "Nombre" 				NVARCHAR(200)	DEFAULT ''	NOT NULL
, "NombreCorto"			NVARCHAR(20)	DEFAULT ''	NOT NULL
, "Estado"				NVARCHAR(3) 	DEFAULT ''	NOT NULL
,PRIMARY KEY ("IdConcepto")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.ConceptoConfiguracion"
( "IdConcepto"			BIGINT			DEFAULT '0'	NOT NULL
, "IdItem" 				BIGINT			DEFAULT '0'	NOT NULL
, "Operacion"			NVARCHAR(1)		DEFAULT ''	NOT NULL
, "Factor"				DECIMAL(26,8) 	DEFAULT '0'	NOT NULL
,PRIMARY KEY ("IdConcepto", "IdItem", "Operacion")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.ItemTipo"
( "IdTipoItem"			NVARCHAR(3)		DEFAULT ''	NOT NULL
, "Nombre" 				NVARCHAR(200)	DEFAULT ''	NOT NULL
, "Orden"				BIGINT			DEFAULT '0'	NOT NULL
, "Estado"				NVARCHAR(3) 	DEFAULT ''	NOT NULL
,PRIMARY KEY ("IdTipoItem")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.ItemConfiguracion"
( "IdConfiguracionItem"		BIGINT			DEFAULT '0'	NOT NULL
, "IdItem" 					BIGINT			DEFAULT '0'	NOT NULL
, "IdProcesoFuncion"		BIGINT			DEFAULT '0'	NOT NULL
, "CuentaContable"			NVARCHAR(12) 	DEFAULT ''	NOT NULL
, "SaldoDebeHaber"			NVARCHAR(1)		DEFAULT ''	NOT NULL
, "Condicion" 				NVARCHAR(1)		DEFAULT ''	NOT NULL
, "Monto"					DECIMAL(26,8)	DEFAULT '0'	NOT NULL
, "Operacion"				NVARCHAR(1) 	DEFAULT ''	NOT NULL
, "Factor"					DECIMAL(26,8)	DEFAULT '0'	NOT NULL
, "MayorACero" 				NVARCHAR(1)		DEFAULT ''	NOT NULL
, "ManorACero"				NVARCHAR(1)		DEFAULT ''	NOT NULL
, "PermiteCero"				NVARCHAR(1) 	DEFAULT ''	NOT NULL
, "Estado"					NVARCHAR(3) 	DEFAULT ''	NOT NULL
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.ProcesoFuncion"
(
"IdProcesoFuncion"			BIGINT 			DEFAULT '0' NOT NULL
,"Nombre"					NVARCHAR(50) 	DEFAULT '' 	NOT NULL
,"Nemo"						NVARCHAR(20) 	DEFAULT '' 	NOT NULL
,"Orden"					BIGINT			DEFAULT '0' NOT NULL
,"Estado"					NVARCHAR(3)		DEFAULT '' 	NOT NULL
,PRIMARY KEY ("IdProcesoFuncion")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.HomologaCuenta" 
(
 "CuentaOptimus" 			NVARCHAR(15) 	DEFAULT '' NOT NULL,
 "CuentaContabilidad" 		NVARCHAR(15) 	DEFAULT '' NOT NULL,
 "CodigoContabilidad" 		NVARCHAR(15) 	DEFAULT '' NOT NULL ,
 "CuentaProveedor" 			NVARCHAR(12) 	DEFAULT '' NOT NULL ,
 "CuentaEEFF" 				NVARCHAR(12) 	DEFAULT '' NOT NULL ,
 "CentroCosto" 				NVARCHAR(12) 	DEFAULT '' NOT NULL ,
 "NombreCuenta" 			NVARCHAR(70) 	DEFAULT '' NOT NULL ,
 "DetalleCuenta" 			NVARCHAR(70) 	DEFAULT '' NOT NULL ,
 PRIMARY KEY ("CuentaOptimus")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.p_SaldoCuentaContable" 
(
 "IdCodigoContabilidad" 	NVARCHAR(15)  	DEFAULT '' NOT NULL,
 "MontoDebe" 				DECIMAL(18,2) 	DEFAULT '0',
 "MontoHaber" 				DECIMAL(18,2) 	DEFAULT '0',
 PRIMARY KEY ("IdCodigoContabilidad")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.InformeResultadoItemDig" 
(
 "IdTipoInforme" 			BIGINT 			NOT NULL, 
 "NombreInforme" 			NVARCHAR(200) 	NOT NULL, 
 "OrdenInforme" 			INT 			NOT NULL, 
 "IdEmpresa" 				INT 			NOT NULL, 
 "FechaContable" 			NVARCHAR(10) 	NOT NULL, 
 "IdIndice" 				INT 			NOT NULL, 
 "NombreIndice" 			NVARCHAR(200) 	NOT NULL,
 "PosicionIndice" 			NVARCHAR(1) 	NOT NULL, 
 "OperadorIndice" 			INT 			NOT NULL, 
 "FactorIndice" 			DECIMAL(5,3) 	NOT NULL, 
 "IdConcepto" 				INT 			NOT NULL, 
 "NombreConcepto" 			NVARCHAR(200) 	NOT NULL, 
 "OperacionConcepto" 		INT 			NOT NULL, 
 "FactorConcepto" 			DECIMAL(5,3) 	NOT NULL, 
 "IdItem" 					INT 			NOT NULL, 
 "NombreItem" 				NVARCHAR(200) 	NOT NULL, 
 "IdTipoItem" 				NVARCHAR(3) 	NOT NULL, 
 "MayorACeroItem" 			NVARCHAR(1) 	NOT NULL, 
 "MenorACeroItem" 			NVARCHAR(1) 	NOT NULL, 
 "PermiteCeroItem" 			NVARCHAR(1) 	NOT NULL, 
 "Monto" 					DECIMAL(20,4) 	NOT NULL,
 CONSTRAINT "PK_ILS_InformeResultadoItemDig" PRIMARY KEY INVERTED VALUE ("IdTipoInforme", "IdEmpresa", "FechaContable", "IdIndice", "OperadorIndice", "FactorIndice", "IdConcepto", "OperacionConcepto", "FactorConcepto", "IdItem", "PosicionIndice")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::ILS.InformeConfiguracion" 
(
 "IdTipoInforme" 			BIGINT 			NOT NULL,
 "IdIndice" 				INT 			NOT NULL,
 "Orden" 					INT 			NOT NULL,
 CONSTRAINT "PK_ILS_InformeConfiguracion" PRIMARY KEY INVERTED VALUE ("IdTipoInforme", "IdIndice")
);

