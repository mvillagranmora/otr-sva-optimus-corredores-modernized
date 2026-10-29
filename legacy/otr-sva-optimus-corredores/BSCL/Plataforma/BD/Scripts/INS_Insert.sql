SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
/*DELETE FROM  "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento";
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento";
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoOperacion";  

DELETE FROM  "BSCL.Plataforma.BD::INS.TipoInstrumento"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion"
DELETE FROM  "BSCL.Plataforma.BD::INS.ProductoLineaRiesgo"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.ProductoEmpresa"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.MercadoYBolsa"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.Mercado"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.Instrumento"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.ExigenciaProducto"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.EmisorYDocumentoIdentidad"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.DiversificacionProducto"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.CoberturaProducto"; 
DELETE FROM  "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento";  
DELETE FROM  "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento";
DELETE FROM  "BSCL.Plataforma.BD::INS.Bolsa";  
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoProducto";
DELETE FROM  "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ; 
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo";
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo";
DELETE FROM  "BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio";
DELETE FROM  "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste";
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento";
DELETE FROM  "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"*/
/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INAX','INAX','Acciones extranjeras en BCS','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INAC','INAC','Intermediacion acciones','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INCI','INCI','Intermediacion CFI','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INCM','INCM','Intermediacion CFM','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INCX','INCX','Intermedacion CFI Extranjeros','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INEN','INEN','Intermediacion ETF nacional','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INEX','INEX','Intermediacion ETF extranjero','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INFU','INFU','Intermediacion futuros','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INFM','INFM','Intermediacion fondos mutuos','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INIF','INIF','Intermediacion IIF','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INRF','INRF','Intermediacion renta fija','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INSI','INSI','Intermediación simultáneas','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('MEFW','MEFW','Mesa forward','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('MEME','MEME','Mesa moneda extranjera (Spot)','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('MERF','MERF','Mesa pactos','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('CUST','CUST','Custodia de titulos','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INMO','INMO','Intermediación Monetarios','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INMI','INMI','Intermediación MILA','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('INOS','INOS','Intermediacion OSAs','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ( "IdTipoProducto","Codigo","Nombre","IdEstado" )  
VALUES('TESO','TESO','TESORERIA','VIG');
 

INSERT INTO "BSCL.Plataforma.BD::INS.Bolsa" ( "IdBolsa","Codigo","Nombre","IdPais","IdEstado" )  
VALUES('BCS','BCS','Bolsa de comercio de Santiago',10,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.Bolsa" ( "IdBolsa","Codigo","Nombre","IdPais","IdEstado" )  
VALUES('BEC','BEC','Bolsa electronica',10,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.Bolsa" ( "IdBolsa","Codigo","Nombre","IdPais","IdEstado" )  
VALUES('BCV','BCV','Bolsa de comercio de Valparaiso',10,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.Bolsa" ( "IdBolsa","Codigo","Nombre","IdPais","IdEstado" )  
VALUES('BVL','BVL','Bolsa de valores de Lima',34,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.Bolsa" ( "IdBolsa","Codigo","Nombre","IdPais","IdEstado" )  
VALUES('BVC','BVC','Bolsa de valores de Colombia',12,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.Bolsa" ( "IdBolsa","Codigo","Nombre","IdPais","IdEstado" )  
VALUES('FRD','FRD','Fuera de rueda',10,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.Bolsa" ( "IdBolsa","Codigo","Nombre","IdPais","IdEstado" )  
VALUES('BMV','BMV','Bolsa mexicana de comercio',137,'ACT');




INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(1,'ACN','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(2,'ACX','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(3,'CFI','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(4,'CFM','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(5,'ETN','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(6,'ETX','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(7,'MON','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(8,'CFX','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(9,'SIM','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(10,'PRT','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(11,'ACN','BEC','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(12,'ACN','BCV','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(13,'ACN','FRD','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(14,'ACX','BVL','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(15,'ACX','BVC','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(16,'ETN','BEC','','N',100,'ACT');

INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(17,'BBC','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(18,'BCD','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(19,'BCP','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(20,'BCU','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(21,'BEM','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(22,'CER','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(23,'FRW','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(24,'LTH','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(25,'PAC','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(26,'PAV','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(27,'PRC','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(28,'ZER','BCS','','N',100,'ACT');
/*
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(29,'PGN','BCS','','N',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(30,'PGR','BCS','','N',100,'ACT');
*/

INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(30,'PRT','BCS','DCV','S',100,'ACT');

INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(31,'BCA','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(32,'BCX','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(33,'BTP','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(34,'BTU','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(35,'BUS','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(36,'BVL','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(37,'COR','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(38,'PCX','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(39,'PRD','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(40,'XER','BCS','DCV','S',100,'ACT');

INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(41,'FDO','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(42,'FIP','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(43,'FBC','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(44,'FIC','BCS','DCV','S',100,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
VALUES(45,'FUF','BCS','DCV','S',100,'ACT');

INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
select max("IdBolsaTipoInstrumento")+1,'PAG','BCS','','N',100,'ACT' from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento";

INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" 
("IdBolsaTipoInstrumento","IdTipoInstrumento","IdBolsa","IdBoveda","ExigeGarantia","IdContraparteGarantia","IdEstado" )  
select max("IdBolsaTipoInstrumento")+1,'OSA','BCS','','N',100,'ACT' from "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento";





INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ( "IdClasificacionTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('RV','RV','Renta Variable','Renta Variable','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ( "IdClasificacionTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('RF','RF','Renta Fija','Renta Fija','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ( "IdClasificacionTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('IF','IF','Intermediacion Financiera','Intermediacion Financiera','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ( "IdClasificacionTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('DV','DV','Derivados','Derivados','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ( "IdClasificacionTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('FM','FM','Fondos Mutuos','Fondos Mutuos','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ( "IdClasificacionTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('MO','MO','Monetarios','Monetarios','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" ( "IdClasificacionTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('SP','SPT','Spot','Spot','ACT');

select * from "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" 

INSERT INTO "BSCL.Plataforma.BD::INS.Mercado" ( "IdMercado","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('BSCL','BCCL','BCCL','Bolsa de santiago de Chile','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.Mercado" ( "IdMercado","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('MILA','MILA','MILA','Mercado integrado latinoamericano','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.Mercado" ( "IdMercado","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('BECL','BECL','BECL','Bolsa electronica de Chile','VIG');
INSERT INTO "BSCL.Plataforma.BD::INS.Mercado" ( "IdMercado","Codigo","Nombre","Descripcion","IdEstado" )  
VALUES('BVCL','BVCL','BVCL','Bolsa de valparaiso de Chile','VIG');

INSERT INTO "BSCL.Plataforma.BD::INS.MercadoYBolsa" ( "IdMercadoYBolsa","IdMercado","IdBolsa" )  
VALUES(1,'MILA','BCS');
INSERT INTO "BSCL.Plataforma.BD::INS.MercadoYBolsa" ( "IdMercadoYBolsa","IdMercado","IdBolsa" )  
VALUES(2,'MILA','BVL');
INSERT INTO "BSCL.Plataforma.BD::INS.MercadoYBolsa" ( "IdMercadoYBolsa","IdMercado","IdBolsa" )  
VALUES(3,'MILA','BVC');
INSERT INTO "BSCL.Plataforma.BD::INS.MercadoYBolsa" ( "IdMercadoYBolsa","IdMercado","IdBolsa" )  
VALUES(4,'MILA','BMV');
INSERT INTO "BSCL.Plataforma.BD::INS.MercadoYBolsa" ( "IdMercadoYBolsa","IdMercado","IdBolsa" )  
VALUES(5,'BCCL','BCS');
INSERT INTO "BSCL.Plataforma.BD::INS.MercadoYBolsa" ( "IdMercadoYBolsa","IdMercado","IdBolsa" )  
VALUES(6,'BECL','BEC');
INSERT INTO "BSCL.Plataforma.BD::INS.MercadoYBolsa" ( "IdMercadoYBolsa","IdMercado","IdBolsa" )  
VALUES(7,'BVCL','BCV');


INSERT INTO "BSCL.Plataforma.BD::INS.ProductoEmpresa" ( "IdProductoEmpresa","IdTipoProducto","IdEmpresa","IdEstado" )  
VALUES(1,'INAC',1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ProductoEmpresa" ( "IdProductoEmpresa","IdTipoProducto","IdEmpresa","IdEstado" )  
VALUES(2,'INAX',1,'ACT');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ORV','ORV','OTROS INSTRUMENTOS RENTA VARIABLE','OTROS INSTRUMENTOS RENTA VARIABLE','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ACN','AC_NAC','ACCIONES NACIONALES','ACCIONES NACIONALES','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ACX','AC_EXT','ACCIONES EXTRANJERAS','ACCIONES EXTRANJERAS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('MIL','AC_MIL','ACCIONES MILA','ACCIONES MILA','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('CFI','CFI','CUOTAS DE FONDOS DE INVERSION','CUOTAS DE FONDOS DE INVERSION','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('CFM','CFM','CUOTAS DE FONDOS MUTUOS','CUOTAS DE FONDOS MUTUOS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('CFX','CFX','CUOTAS DE FONDOS DE INVERSION EXTRANJEROS','CUOTAS DE FONDOS DE INVERSION EXTRANJEROS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('DIV','DIVISA','DIVISAS','DIVISAS','VIG','MO');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('EMP','EMPRESTITO','EMPRESTITO','EMPRESTITO','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ETN','ETF_NAC','ETF NACIONALES','ETF NACIONALES','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ETX','ETF_EXT','ETF EXTRANJEROS','ETF EXTRANJEROS','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FFM','FFMM','FONDOS MUTUOS','FONDOS MUTUOS','VIG','FM');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('MON','MON','MONETARIOS','MONETARIOS','VIG','RV');
--INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
--VALUES('PAC','PAC','COMPRA PACTOS','COMPRA CON PACTOS','VIG','RF');
--INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
--VALUES('PAV','PAV','VENTA PACTOS','VENTA CON PACTOS','VIG','RF');
/*
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PGN','PAG_NR','PAGARE NO REAJUSTABLE','PAGARE NO REAJUSTABLE','VIG','IF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PGR','PAG_R','PAGARE REAJUSTABLE','PAGARE REAJUSTABLE','VIG','IF');*/
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PAG','PAGARE','PAGARE','PAGARE','VIG','IF');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PRT','PRESTAMO','PRESTAMOS','PRESTAMOS','VIG','RV');
--INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
--VALUES('SIC','SIC','SIMULTÁNEA COMPRA','SIMULTÁNEA DE COMPRA','VIG','RV');
--INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
--VALUES('SIV','SIV','SIMULTÁNEA VENTA','SIMULTÁNEA DE VENTA','VIG','RV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('SIM','SIM','SIMULTÁNEA','SIMULTÁNEA','VIG','RV');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FDO','FUT_DOLAR','FUTUROS DOLAR','FUTUROS DOLAR','VIG','DV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FIP','FUT_IPSA','FUTUROS IPSA','FUTUROS IPSA','VIG','DV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FBC','FUT_BCU','FUTUROS BCU','FUTUROS BCU','VIG','DV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FIC','FUT_ICP','FUTUROS ICP','FUTUROS ICP','VIG','DV');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FUF','FUT_UF','FUTUROS UF','FUTUROS UF','VIG','DV');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ORF','ORF','OTROS INSTRUMENTOS RENTA FIJA','OTROS INSTRUMENTOS RENTA FIJA','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BBC','BB','BONOS BANCARIOS','BONOS BANCARIOS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCD','BCD','BANCO CENTRAL BONOS BULLET EN DOLARES','BANCO CENTRAL BONOS BULLET EN DOLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCP','BCP','BANCO CENTRAL BONOS BULLET EN PESOS','BANCO CENTRAL BONOS BULLET EN PESOS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCU','BCU','BANCO CENTRAL BONOS BULLET EN UF','BANCO CENTRAL BONOS BULLET EN UF','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BEM','BE','BONOS EMPRESAS','BONOS EMPRESAS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('CER','CERO','CUPONES DE EMISION REAJUSTABLES EN UF','CUPONES DE EMISION REAJUSTABLES EN UF','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('FRW','FORW','FORWARDS','FORWARDS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('LTH','LH','LETRAS HIPOTECARIAS','LETRAS HIPOTECARIAS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PRC','PRC','PAGARE REAJUSTABLE DEL BANCO CENTRAL','PAGARE REAJUSTABLE DEL BANCO CENTRAL','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('ZER','ZERO','CUPONES DE EMISION REAJUSTABLES EN DOLARES','CUPONES DE EMISION REAJUSTABLES EN DOLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PAC','PAC','PACTOS','PACTOS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCA','BC','BONO CACSA','BONO CACSA','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BCX','BCX','BONOS BANCO CENTRAL EN DÓLARES','BONOS BANCO CENTRAL EN DÓLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BTP','BTP','BONO TESORERÍA GENERAL DE LA REPÚBLICA DE CHILE EN PESOS','BONO TESORERÍA GENERAL DE LA REPÚBLICA DE CHILE EN PESOS','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BTU','BTU','BONO TESORERÍA GENERAL DE LA REPÚBLICA DE CHILE EN UF','BONO TESORERÍA GENERAL DE LA REPÚBLICA DE CHILE EN UF','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BUS','BU','BONO SUBORDINADO','BONO SUBORDINADO','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BVL','BVL','MINISTERIO DE VIVIENDO Y URBANISMO LEASING','MINISTERIO DE VIVIENDO Y URBANISMO LEASING','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('COR','CORA','BONO REFORMA AGRARIA','BONO REFORMA AGRARIA','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PCX','PCX','PAGARÉS BANCO CENTRAL EN DÓLARES','PAGARÉS BANCO CENTRAL EN DÓLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PRD','PRD','PAGARÉS REAJUSTABLES DEL BANCO CENTRAL DE CHILE EN DÓLARES','PAGARÉS REAJUSTABLES DEL BANCO CENTRAL DE CHILE EN DÓLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('XER','XERO','CUPONES DE EMISIÓN DEL BANCO CENTRAL DE CHILE EN DÓLARES','CUPONES DE EMISIÓN DEL BANCO CENTRAL DE CHILE EN DÓLARES','VIG','RF');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumento" ( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('OSA','OSA','OPCIONES DE SUSCRIPCION DE ACCIONES','OPCIONES DE SUSCRIPCION DE ACCIONES','VIG','RV');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(1,'BBC','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(2,'BBC','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(3,'BBC','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(4,'BCD','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(5,'BCP','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(6,'BCU','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(7,'BEM','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(8,'BEM','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(9,'BEM','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(10,'CER','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(11,'FRW','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(12,'LTH','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(13,'LTH','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(14,'LTH','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(15,'PRC','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(16,'ZER','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(17,'PAC','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(18,'BCA','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(19,'BCA','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )
VALUES(20,'BCA','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(21,'BCX','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(22,'BTP','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(23,'BTU','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(24,'BUS','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(25,'BVL','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(26,'COR','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(27,'PCX','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(28,'PRD','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(29,'XER','USD','CLP');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(30,'ACN','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(31,'ACX','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(32,'ACX','EUR','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(33,'MIL','PEN','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(34,'MIL','COL','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(35,'ACN','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(36,'MIL','USD','CLP');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(37,'CFI','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(38,'CFM','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(39,'CFX','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(40,'ETN','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(41,'ETX','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(42,'FDO','CLP','CLP');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(43,'FUF','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(44,'FIP','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(45,'MON','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(46,'FBC','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(47,'FIC','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(48,'OSA','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(49,'PAG','USD','CLP');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(50,'PAG','UF','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(51,'PAG','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(52,'PAG','EUR','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(53,'ACX','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(54,'CFX','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(55,'CFX','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(56,'CFX','EUR','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(57,'PAG','EUR','USD');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(58,'ETX','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(59,'FDO','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(60,'FBC','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(61,'SIM','CLP','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(62,'MIL','USD','USD');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(63,'MIL','PEN','USD');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(64,'ETN','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(65,'ACX','USD','CLP');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(66,'MIL','USD','USD');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion" )  
VALUES(67,'ETX','USD','CLP');



INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion") 
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+1,'PAG','USD','CLP' FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" UNION ALL
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+2,'PAG','UF','CLP'  FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" UNION ALL
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+3,'PAG','CLP','CLP' FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" UNION ALL

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion") 
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+1,'FDO','USD','CLP' FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" UNION ALL
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+2,'FBC','USD','CLP'  FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" 
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+3,'FUF','CLP','CLP' FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" UNION ALL
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+4,'FIC','CLP','CLP'  FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" 
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+5,'FIP','CLP','CLP'  FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" 

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" ( "IdTipoInstrumentoMonedaLiquidacion","IdTipoInstrumento","IdMoneda","IdMonedaLiquidacion") 
SELECT MAX("IdTipoInstrumentoMonedaLiquidacion")+1,'SIM', 'CLP','CLP' FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" UNION ALL

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(1,'FIP','BCS','FIP','ACT',0,'');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(2,'FDO','BCS','FDO','ACT',0,'');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(3,'FIC','BCS','FIC','ACT',0,'');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(4,'FBC','BCS','FBC','ACT',0,'');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(5,'FUF','BCS','FUF','ACT',0,'');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(6,'SIM','BCS','ACN','ACT',2249,'SIMULTÁNEA VENTA');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(7,'SIM','BCS','ETN','ACT',2249,'SIMULTÁNEA VENTA');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(8,'SIM','BCS','ETX','ACT',2249,'SIMULTÁNEA VENTA');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(9,'SIM','BCS','CFI','ACT',2249,'SIMULTÁNEA VENTA');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(10,'PRT','BCS','ACN','ACT',0,'');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(11,'PRT','BCS','ETN','ACT',0,''); 
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(12,'PRT','BCS','ETX','ACT',0,''); 
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
VALUES(13,'PRT','BCS','CFI','ACT',0,''); 

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente"("IdInstrumentoSubyacente","IdTipoInstrumento","IdBolsa","IdTipoInstrumentoSubyacente","IdEstado","IdInstrumento","Nemotecnico")
select (13 + row_number() over(order by ("IdTipoInstrumento"))), 'PAC','BCS', "IdTipoInstrumento",'ACT',0,''
from "BSCL.Plataforma.BD::INS.TipoInstrumento"
where "IdClasificacionTipoInstrumento" in ('RF','IF') and "IdTipoInstrumento" !='PAC';


insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMACX','COMACX','COMPRA ACCIONES EXTRANJERAS','COMPRA','INAX','S','S','N','N','S','COMPRA','COMPRA','N','VENACX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENACX','VENACX','VENTA ACCIONES EXTRANJERAS','VENTA','INAX','S','S','N','N','S','VENTA','VENTA','N','COMACX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMSIM','COMSIM','COMPRA DE SIMULTÁNEA','COMPRA','INSI','S','S','N','N','S','COMPRA','COMPRA','N','VENSIM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENSIM','VENSIM','VENTA DE SIMULTÁNEA','VENTA','INSI','S','S','N','N','S','VENTA','VENTA','N','COMSIM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMCFI','COMCFI','COMPRA CFI NACIONAL','COMPRA','INCI','S','S','N','N','S','COMPRA','COMPRA','N','VENCFI');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENCFI','VENCFI','VENTA CFI NACIONAL','VENTA','INCI','S','S','N','N','S','VENTA','VENTA','N','COMCFI');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMCFX','COMCFX','COMPRA CFI EXTRANJERO','COMPRA','INCX','S','S','N','N','S','COMPRA','COMPRA','N','VENCFX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENCFX','VENCFX','VENTA CFI EXTRANJERO','VENTA','INCX','S','S','N','N','S','VENTA','VENTA','N','COMCFX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMMON','COMMON','COMPRA MONETARIO','COMPRA','INMO','S','S','N','N','S','COMPRA','COMPRA','N','VENMON');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENMON','VENMON','VENTA MONETARIO','VENTA','INMO','S','S','N','N','S','VENTA','VENTA','N','COMMON');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMETN','COMETN','COMPRA ETF NACIONAL','COMPRA','INEN','S','S','N','N','S','COMPRA','COMPRA','N','VENETN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENETN','VENETN','VENTA ETF NACIONAL','VENTA','INEN','S','S','N','N','S','VENTA','VENTA','N','COMETN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMETX','COMETX','COMPRA ETF EXTRANJERO','COMPRA','INEX','S','S','N','N','S','COMPRA','COMPRA','N','VENETX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENETX','VENETX','VENTA ETF EXTRANJERO','VENTA','INEX','S','S','N','N','S','VENTA','VENTA','N','COMETX');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMCFM','COMCFM','COMPRA CFM','COMPRA','INCM','S','S','N','N','S','COMPRA','COMPRA','N','VENCFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENCFM','VENCFM','VENTA CFM','VENTA','INCM','S','S','N','N','S','VENTA','VENTA','N','COMCFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMPAC','COMPAC','COMPRA CON PACTO','COMPRA','MERF','S','N','N','N','N','COMPRA','COMPRA','N','VENPAC');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENPAC','VENPAC','VENTA CON PACTO','VENTA','MERF','S','N','N','N','N','VENTA','VENTA','N','COMPAC');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMTER','COMTER','COMPRA A TERMINO','COMPRA','MERF','S','N','N','N','N','COMPRA','COMPRA','N','VENTER');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENTER','VENTER','VENTA A TERMINO','VENTA','MERF','S','N','N','N','N','VENTA','VENTA','N','COMTER');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('INGTIT','INGTIT','INGRESO DE TITULO','INGRESO','CUST','N','N','N','N','N','INGRESO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EGRTIT','EGRTIT','EGRESO DE TITULO','EGRESO','CUST','N','N','N','N','N','EGRESO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('TRATIT','TRATIT','TRASPASO DE TITULOS','TRASPASO','CUST','N','N','N','N','N','TRASPASO','','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('PRESTA','PRESTA','PRESTAMO','COMPRA','INAC','N','N','N','N','N','COMPRA','VENTA','N','EMPRES');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EMPRES','EMPRES','EMPRESTITO','VENTA','INAC','N','N','N','N','N','VENTA','COMPRA','N','PRESTA');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMFWD','COMFWD','COMPRA DE FORWARD','COMPRA','MEFW','S','N','N','N','N','COMPRA','COMPRA','N','VENFWD');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENFWD','VENFWD','VENTA DE FORWARD','VENTA','MEFW','S','N','N','N','N','VENTA','VENTA','N','COMFWD');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMSPT','COMSPT','COMPRA SPOT','COMPRA','MEME','S','N','N','N','N','COMPRA','COMPRA','N','VENSPT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENSPT','VENSPT','VENTA SPOT','VENTA','MEME','S','N','N','N','N','VENTA','VENTA','N','COMSPT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('APORFM','APORFM','APORTE FONDO MUTUO','APORTE','INFM','N','N','N','N','N','APORTE','','N','RESCFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('RESCFM','RESCFM','RESCATE FONDO MUTUO','RESCATE','INFM','N','N','N','N','N','RESCATE','','N','APORFM');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('CARMAN','CARMAN','CARGO MANUAL','CARGO','CTIN','N','N','N','N','N','CARGO','','N','ABNMAN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('ABNMAN','ABNMAN','ABONO MANUAL','ABONO','CTIN','N','N','N','N','N','ABONO','','N','CARMAN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('CARTRA','CARTRA','CARGO TRASPASO','CARGO','CTIN','N','N','N','N','N','CARGO','','N','ABNTRA');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMIIF','COMIIF','COMPRA INTERMEDIACION FINANCIERA','COMPRA','INIF','S','N','N','N','S','COMPRA','COMPRA','N','VENIIF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENIIF','VENIIF','VENTA INTERMEDIACION FINANCIERA','VENTA','INIF','S','N','N','N','S','VENTA','VENTA','N','COMIIF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMRF','COMRF','COMPRA MERCADO RENTA FIJA','COMPRA','INRF','S','N','N','N','S','COMPRA','COMPRA','N','VENRF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENRF','VENRF','VENTA MERCADO RENTA FIJA','VENTA','INRF','S','N','N','N','S','VENTA','VENTA','N','COMRF');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMFUT','COMFUT','COMPRA FUTUROS','COMPRA','INFU','S','S','N','N','S','COMPRA','COMPRA','N','VENFUT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENFUT','VENFUT','VENTA FUTUROS','COMPRA','INFU','S','S','N','N','S','VENTA','COMPRA','N','COMFUT');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMACN','COMACN','COMPRA ACCIONES NACIONALES','COMPRA','INAC','S','S','N','N','S','COMPRA','COMPRA','N','VENACN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENACN','VENACN','VENTA ACCIONES NACIONALES','VENTA','INAC','S','S','N','S','S','VENTA','VENTA','N','COMACN');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMMIL','COMMIL','COMPRA ACCIONES MILA','COMPRA','INMI','S','S','N','N','S','COMPRA','COMPRA','N','VENMIL');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENMIL','VENMIL','VENTA ACCIONES MILA','VENTA','INMI','S','S','N','S','S','VENTA','VENTA','N','COMMIL');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('COMOSA','COMOSA','COMPRA OSA','COMPRA','INOS','S','S','N','N','S','COMPRA','COMPRA','N','VENOSA');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('VENOSA','VENOSA','VENTA OSA','VENTA','INOS','S','S','N','N','S','VENTA','VENTA','N','COMOSA');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('INGGTA','INGGTA','INGRESO GARANTIA','INGRESO','CUST','N','N','N','N','N','INGRESO','INGRESO','N','');
insert into "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") values('EGRGTA','EGRGTA','EGRESO GARANTIA','EGRESO','CUST','N','N','N','N','N','EGRESO','EGRESO','N','');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") VALUES('DIVDIN','DIVDIN','DIVIDENDO EN DINERO','ABONO','EVCA','N','N','N','N','N', 'INGRESO', '', 'N', '', 'R' )
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion", "Codigo", "Nombre", "Accion", "IdTipoProducto", "Comprobante", "EsOrden", "EsSimultanea", "EsVentaCorta", "EsTransaccion", "AccionOperacion", "AccionCustodia", "EsIngresoEgresoTitulo", "IdTipoOperacionRelacionada") VALUES('DIVACN','DIVACN','DIVIDENDO EN ACCIONES','INGRESO','EVCA','N','N','N','N','N', 'INGRESO', 'INGRESO', 'N', '', 'R' )

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('ANSIMC','ANSIMC','ANTICIPO SIMULTANEA DE COMPRA', 'VENTA','INSI','N','N','N','N','N','VENTA','RETIRO','ANSIMV','','S','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('ANSIMV','ANSIMV','ANTICIPO SIMULTANEA DE VENTA', 'COMPRA','INSI','N','N','N','N','N','COMPRA','INGRESO','ANSIMC','','S','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('ANTPRT','ANTPRT','ANTICIPO DE PRESTAMO', 'VENTA','INAC','N','N','N','N','N','VENTA','RETIRO','ANTEMP','','S','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('ANTEMP','ANTEMP','ANTICIPO DE EMPRESTITO', 'VENTA','INAC','N','N','N','N','N','VENTA','RETIRO','ANTPRT','','S','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('VESIMC','VESIMC','VENCIMIENTO SIMULTANEA DE COMPRA', 'VENTA','INSI','N','N','N','N','N','VENTA','RETIRO','VESIMV','','N','S','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('VESIMV','VESIMV','VENCIMIENTO SIMULTANEA DE VENTA', 'COMPRA','INSI','N','N','N','N','N','COMPRA','INGRESO','VESIMC','','N','S','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('VENPRT','VENPRT','VENCIMIENTO DE PRESTAMO', 'VENTA','INAC','N','N','N','N','N','VENTA','RETIRO','VENEMP','','N','S','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('VENEMP','VENEMP','VENCIMIENTO DE EMPRESTITO', 'VENTA','INAC','N','N','N','N','N','VENTA','RETIRO','VENPRT','','N','S','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('INGOSA','INGOSA','INGRESO DE OSA', 'INGRESO','INAC','N','N','N','N','N','INGRESO','INGRESO','','','N','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('SUSOSA','SUSOSA','SUSCRIPCION DE OSA', 'COMPRA','INAC','N','N','N','N','N','INGRESO','INGRESO','','','N','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('DIVACC','DIVACC','DIVIDENDO EN ACCIONES', 'COMPRA','INAC','N','N','N','N','N','INGRESO','INGRESO','','','N','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('DIVACG','DIVACG','DIVIDENDO EN ACCIONES GARANTIAS', 'COMPRA','INAC','N','N','N','N','N','INGRESO','INGRESO','','','N','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion") VALUES ('SORLET','SORLET','SORTEO DE LETRAS', 'VENTA','INRF','N','N','N','N','N','INGRESO','','' ,'R' ,'N','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion","EsMovimientoComprobante") VALUES ('EGRTES','EGRTES','EGRESOS DE TESORERIA','EGRESO','TESO','N','N','N','N','N','','','' ,'R','N','N','N','N');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion","EsMovimientoComprobante") VALUES ('INGTES','INGTES','INGRESOS DE TESORERIA','INGRESO','TESO','N','N','N','N','N','','','' ,'P','N','N','N','N');

/*
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(1,'COMACN','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(2,'VENACN','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(3,'COMACX','USD','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(4,'VENACX','USD','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(5,'COMCFI','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(6,'VENCFI','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(7,'COMCFM','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(8,'VENCFM','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(9,'COMETN','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(10,'VENETN','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(11,'COMCFX','USD','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(12,'VENCFX','USD','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(13,'COMMON','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(14,'VENMON','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(15,'COMETX','USD','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(16,'VENETX','USD','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(17,'COMACX','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(18,'VENACX','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(19,'VENSIM','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(20,'COMSIM','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(21,'VENRF','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(22,'COMRF','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(23,'COMFUT','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(24,'VENFUT','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
select max("IdTipoOperacionYMoneda")+1,'COMIIF','CLP','ACT' from "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda";
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
select max("IdTipoOperacionYMoneda")+1,'VENIIF','CLP','ACT' from "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda";
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(29,'COMOSA','CLP','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionYMoneda" ( "IdTipoOperacionYMoneda","IdTipoOperacion","IdMoneda","IdEstado" )  
VALUES(30,'VENOSA','CLP','ACT');
*/

insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(1,'COMACN','ACN','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(2,'VENACN','ACN','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(3,'COMACX','ACX','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(4,'VENACX','ACX','ACT',0,'');

select 5,'COMSIM',"IdTipoInstrumento",'ACT',"IdInstrumento","Nemotecnico" 
from "BSCL.Plataforma.BD::INS.Instrumento" 
where "IdTipoInstrumento"='SIM' and "Nemotecnico" like '%COMPRA%'
into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico");

select 6,'VENSIM',"IdTipoInstrumento",'ACT',"IdInstrumento","Nemotecnico" 
from "BSCL.Plataforma.BD::INS.Instrumento" 
where "IdTipoInstrumento"='SIM' and "Nemotecnico" like '%VENTA%'
into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico");

insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(7,'COMCFI','CFI','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(8,'VENCFI','CFI','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(9,'COMCFM','CFM','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(10,'VENCFM','CFM','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(11,'COMETN','ETN','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(12,'VENETN','ETN','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(13,'COMETX','ETX','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(14,'VENETX','ETX','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(15,'COMMON','MON','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(16,'VENMON','MON','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(17,'COMCFX','CFX','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(18,'VENCFX','CFX','ACT',0,'');

insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(21,'COMFUT','FDO','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(22,'VENFUT','FDO','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(23,'COMFUT','FIC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(24,'VENFUT','FIC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(25,'COMFUT','FIP','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(26,'VENFUT','FIP','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(27,'COMFUT','FBC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(28,'VENFUT','FBC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(29,'COMFUT','FUF','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(30,'VENFUT','FUF','ACT',0,'');

insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(31,'COMRF','BBC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(32,'COMRF','BCD','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(33,'COMRF','BCP','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(34,'COMRF','BCU','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(35,'COMRF','BEM','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(36,'COMRF','CER','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(37,'COMRF','FRW','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(38,'COMRF','LTH','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(39,'COMRF','PAC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(40,'COMRF','PAV','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(41,'COMRF','PRC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(42,'COMRF','ZER','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(43,'VENRF','BBC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(44,'VENRF','BCD','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(45,'VENRF','BCP','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(46,'VENRF','BCU','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(47,'VENRF','BEM','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(48,'VENRF','CER','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(49,'VENRF','FRW','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(50,'VENRF','LTH','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(51,'VENRF','PAC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(52,'VENRF','PAV','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(53,'VENRF','PRC','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(54,'VENRF','ZER','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(55,'COMRF','BCA','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(56,'COMRF','BCX','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(57,'COMRF','BTP','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(58,'COMRF','BTU','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(59,'COMRF','BUS','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(60,'COMRF','BVL','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(61,'COMRF','COR','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(62,'COMRF','PCX','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(63,'COMRF','PRD','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(64,'COMRF','XER','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(65,'VENRF','BCA','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(66,'VENRF','BCX','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(67,'VENRF','BTP','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(68,'VENRF','BTU','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(69,'VENRF','BUS','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(70,'VENRF','BVL','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(71,'VENRF','COR','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(72,'VENRF','PCX','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(73,'VENRF','PRD','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(74,'VENRF','XER','ACT',0,'');

insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(75,'COMIIF','PAG','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(76,'VENIIF','PAG','ACT',0,'');

insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(77,'VENMIL','MIL','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(78,'COMMIL','MIL','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(79,'PRESTA','PRT','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(80,'EMPRES','EMP','ACT',0,'');

insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(81,'INGTIT','ACN','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(82,'INGTIT','ACX','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(83,'INGTIT','ETN','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(84,'EGRTIT','ACN','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(85,'EGRTIT','ETN','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(86,'TRATIT','ACN','ACT',0,'');

insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(87,'INGTIT','PAG','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(88,'EGRTIT','PAG','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(89,'INGTIT','BEM','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(90,'EGRTIT','BEM','ACT',0,'');

insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(91,'COMOSA','OSA','ACT',0,'');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
values(92,'VENOSA','OSA','ACT',0,'');


INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (193,'INGTIT','BBC','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (194,'INGTIT','BCA','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (195,'INGTIT','BCD','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (196,'INGTIT','BCP','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (197,'INGTIT','BCU','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (198,'INGTIT','BCX','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (199,'INGTIT','BH' ,'ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (200,'INGTIT','BR' ,'ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (201,'INGTIT','BTP','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (202,'INGTIT','BTU','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (203,'INGTIT','BUS','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (204,'INGTIT','BVL','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (205,'INGTIT','CER','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (206,'INGTIT','COR','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (207,'INGTIT','FRW','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (208,'INGTIT','LTH','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (209,'INGTIT','PAC','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (210,'INGTIT','PCX','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (211,'INGTIT','PRD','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (212,'INGTIT','XER','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (213,'INGTIT','ZER','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (214,'EGRTIT','BBC','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (215,'EGRTIT','BCA','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (216,'EGRTIT','BCD','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (217,'EGRTIT','BCP','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (218,'EGRTIT','BCU','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (219,'EGRTIT','BCX','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (220,'EGRTIT','BH' ,'ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (221,'EGRTIT','BR' ,'ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (222,'EGRTIT','BTP','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (223,'EGRTIT','BTU','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (224,'EGRTIT','BUS','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (225,'EGRTIT','BVL','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (226,'EGRTIT','CER','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (227,'EGRTIT','COR','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (228,'EGRTIT','FRW','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (229,'EGRTIT','LTH','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (230,'EGRTIT','PAC','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (231,'EGRTIT','PCX','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (232,'EGRTIT','PRD','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (233,'EGRTIT','XER','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (234,'EGRTIT','ZER','ACT', 0, '');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (237,'INGTIT','MIL','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (238,'EGRTIT','MIL','ACT', 0, '');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (239,'INGOSA','ACN','ACT', 0, '');

-- faltan del 240 al 245

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (246,'SUSOSA','ACN','ACT', 0, '');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (254,'ANSIMC','SIM','ACT', 0, '');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (255,'ANSIMV','SIM','ACT', 0, '');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (256,'INGCAN','ACN','ACT',0,'');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento", "IdTipoOperacion", "IdTipoInstrumento", "IdEstado", "IdInstrumento", "Nemotecnico") 
VALUES (257,'INGDIV','ACN','ACT',0,'');

--pacto y operacion a termino
--insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
--values(20,'COMTER','BEM','ACT',0,'');
delete from "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" where "IdTipoOperacion" in ('COMTER','VENTER','COMPAC','VENPAC');
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" ("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
select  (257 + row_number() over(order by (a."IdTipoInstrumento"))) as "ID"
	,b."IdTipoOperacion"
	,a."IdTipoInstrumento"
	,'ACT',0,''
from "BSCL.Plataforma.BD::INS.TipoInstrumento" a ,"BSCL.Plataforma.BD::INS.TipoOperacion" b
where a."IdClasificacionTipoInstrumento"in ('RF','IF') and a."IdTipoInstrumento" != 'PAC'
	and b."IdTipoOperacion" in ('COMTER','VENTER')
union all
select  (299 + row_number() over(order by (a."IdTipoInstrumento"))) as "ID"
	,b."IdTipoOperacion"
	,a."IdTipoInstrumento"
	,'ACT',0,''
from "BSCL.Plataforma.BD::INS.TipoInstrumento" a ,"BSCL.Plataforma.BD::INS.TipoOperacion" b
where a."IdClasificacionTipoInstrumento" in ('RF','IF') --and a."IdTipoInstrumento" = 'PAC'
	and b."IdTipoOperacion" in ('COMPAC','VENPAC');

Update a
set a."IdInstrumento"= b."IdInstrumento"
from "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" a, "BSCL.Plataforma.BD::INS.Instrumento" b
where a."IdTipoOperacion"='COMPAC' and a."IdTipoInstrumento" = 'PAC' and b."IdTipoInstrumento" = 'PAC' and b."Nemotecnico" = 'PACTO COMPRA';
Update a
set a."IdInstrumento"= b."IdInstrumento"
from "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" a, "BSCL.Plataforma.BD::INS.Instrumento" b
where a."IdTipoOperacion"='VENPAC' and a."IdTipoInstrumento" = 'PAC' and b."IdTipoInstrumento" = 'PAC' and b."Nemotecnico" = 'PACTO VENTA';


-- 17/02/2016 -- Nuevos reportado como bug jira: IMPCREDOPT-146
insert into "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" (
"IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
select 351,'INGTIT','CFM','ACT',0,'' from dummy union all
select 352,'RETTIT','CFM','ACT',0,'' from dummy union all
select 353,'INGTIT','CFX','ACT',0,'' from dummy union all
select 354,'RETTIT','CFX','ACT',0,'' from dummy union all
select 355,'INGTIT','CFI','ACT',0,'' from dummy union all
select 356,'RETTIT','CFI','ACT',0,'' from dummy union all
select 357,'INGTIT','MIL','ACT',0,'' from dummy union all
select 358,'RETTIT','MIL','ACT',0,'' from dummy union all
select 359,'INGTIT','ETX','ACT',0,'' from dummy union all
select 360,'RETTIT','ETX','ACT',0,'' from dummy union all
select 361,'INGTIT','OSA','ACT',0,'' from dummy union all
select 362,'RETTIT','OSA','ACT',0,'' from dummy union all
select 363,'INGTIT','ORV','ACT',0,'' from dummy union all
select 364,'RETTIT','ORV','ACT',0,'' from dummy;





INSERT INTO "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" ("IdTipoClasificacionRiesgo","IdClasificacionTipoInstrumento","Codigo","Nombre","IdEstado")
VALUES ('CLRF','RF','','Clasificación de Riesgo para mercado RF','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" ("IdTipoClasificacionRiesgo","IdClasificacionTipoInstrumento","Codigo","Nombre","IdEstado")
VALUES ('CLIF','IF','','Clasificación de Riesgo para mercado IF','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" ("IdTipoClasificacionRiesgo","IdClasificacionTipoInstrumento","Codigo","Nombre","IdEstado")
VALUES ('CLRV','RV','','Clasificación de Riesgo para mercado RV','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" ("IdTipoClasificacionRiesgo","IdClasificacionTipoInstrumento","Codigo","Nombre","IdEstado")
VALUES ('CLMO','MO','','Clasificación de Riesgo Mercado Monetarios MO','ACT');



INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (1,'CLRV','A',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (2,'CLRV','B',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (3,'CLRV','C',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (4,'CLIF','N1',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (5,'CLIF','N2',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (6,'CLIF','N3',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (7,'CLIF','N4',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (8,'CLIF','N5',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (9,'CLRF','A',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (10,'CLRF','A+',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (11,'CLRF','A-',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (12,'CLRF','AA',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (13,'CLRF','AA+',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (14,'CLRF','AA-',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (15,'CLRF','AAA',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (16,'CLRF','AAA+',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (17,'CLRF','AAA-',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (18,'CLRF','B',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (19,'CLRF','B+',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (20,'CLRF','B-',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (21,'CLRF','BB',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (22,'CLRF','BB+',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (23,'CLRF','BB-',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (24,'CLRF','BBB',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (25,'CLRF','BBB+',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" ("IdClasificacionRiesgo","IdTipoClasificacionRiesgo","ClasificacionRiesgo","Nivel","IdEstado")
VALUES (26,'CLRF','BBB-',0,'ACT');

INSERT INTO "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento"("IdGrupoInstrumento","CodigoClasificacion","GlosaCodigoClasificacion","IdTipoInstrumento")
VALUES (1,'FUTUROS','GRUPO FUTUROS','FDO');
INSERT INTO "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento"("IdGrupoInstrumento","CodigoClasificacion","GlosaCodigoClasificacion","IdTipoInstrumento")
VALUES (2,'FUTUROS','GRUPO FUTUROS','FIC');
INSERT INTO "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento"("IdGrupoInstrumento","CodigoClasificacion","GlosaCodigoClasificacion","IdTipoInstrumento")
VALUES (3,'FUTUROS','GRUPO FUTUROS','FIP');
INSERT INTO "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento"("IdGrupoInstrumento","CodigoClasificacion","GlosaCodigoClasificacion","IdTipoInstrumento")
VALUES (4,'FUTUROS','GRUPO FUTUROS','FBC');
INSERT INTO "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento"("IdGrupoInstrumento","CodigoClasificacion","GlosaCodigoClasificacion","IdTipoInstrumento")
VALUES (5,'FUTUROS','GRUPO FUTUROS','FUF');


INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (1,'CLIF','PAG',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (3,'CLRV','ACN',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (4,'CLRV','ACX',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (5,'CLRV','CFI',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (6,'CLRV','CFM',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (7,'CLRV','EMP',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (8,'CLRV','ETN',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (9,'CLRV','ETX',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (10,'CLRV','PRT',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (11,'CLRV','SIM',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (12,'CLRF','BBC',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (13,'CLRF','BCD',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (14,'CLRF','BCP',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (15,'CLRF','BCU',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (16,'CLRF','BEM',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (17,'CLRF','CER',0,'ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (18,'CLRF','LTH',0,'ACT');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" ("IdTipoInstrumentoClasificacionRiesgo","IdTipoClasificacionRiesgo","IdTipoInstrumento","Nivel","IdEstado")
VALUES (22,'CLRF','ZER',0,'ACT');

delete from "BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio";

insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(1,'ORDENFUTURO','ORD','','','Ingreso Orden Futuro','CIERRE','BCS','DV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(2,'ORDENACCIONES','ORD','','','Ingreso Orden Acciones','CIERRE','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(3,'ORDENSIMULTANEA','ORD','','','Ingreso Orden Simultanea','CIERRE','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(4,'VALCARTERARV','CUS','','','Valorizar Cartera RV','PROMEDIO','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(5,'VALCARTERARF','CUS','','','Valorizar Cartera RF','PROMEDIO','BCS','RF');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(6,'VALGARANTIA','CUS','','','Valorizar Garantía','VECTOR','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(7,'INGRESOGTIA','CUS','','','Ingreso Garantia','VECTOR','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(8,'INGRESOTITULORV','CUS','','','Ingreso Titulo RV','CIERRE','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(9,'INGRESOTITULORF','CUS','','','Ingreso Titulo RF','CIERRE','BCS','RF');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(10,'CONSULTACARTERARV','CUS','','','Consulta Cartera RV','CIERRE','BCS','RV');
insert into "BSCL"."BSCL.Plataforma.BD::INS.ConfiguracionUsoPrecio" ("IdConfiguracionUsoPrecio","CodigoUsoPrecio","IdModulo","IdTipoOperacion","IdConcepto","Descripcion","TipoPrecioDefault","FuentePrecioDefault","IdClasificacionTipoInstrumento") values(11,'CONSULTACARTERARF','CUS','','','Consulta Cartera RF','CIERRE','BCS','RF');

INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'CLP',1,'CLP'
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE NR' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'USD',1,'USD'
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'EUR',1,'EUR'
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'UF',1,'CLP'
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'CLP',5000000,'CLP'
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PDBC' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'UF',5000,'CLP'
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PRBC' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'CLP',5000000,'CLP' 
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PDT' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'UF',5000,'CLP'
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PRT' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'ICP',1,'CLP'
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';
INSERT INTO "BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"
SELECT "IdInstrumento","IdTipoInstrumento",'IVP',1,'CLP'
FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "Nemotecnico" = 'PAGARE R' and "IdTipoInstrumento" = 'PAG';

INSERT INTO BSCL."BSCL.Plataforma.BD::INS.TipoOperacion"("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion","AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion","EsMovimientoComprobante","AccionOrden")
VALUES('VCDTIT','VCDTIT','VENCIMIENTO DE TÍTULOS','VENCIMIENTO','CUST','N','N','N','N','N','','','','','N','S','N','N',null);

INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
	VALUES ('OPSUAC','Opción de Suscripción Acciones','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
	VALUES ('PRIEMI','Primera Emisión','ACT');
INSERT INTO "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento" ("IdTipoRelacionInstrumento","TipoRelacionInstrumento","IdEstado") 
	VALUES ('SEGEMI','Segunda Emisión','ACT');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
