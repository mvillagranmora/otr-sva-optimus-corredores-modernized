
SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
/*
DELETE FROM  "BSCL.Plataforma.BD::PLA.CamaraDeCompensacion";
DELETE FROM  "BSCL.Plataforma.BD::PLA.TipoMovimiento";
DELETE FROM  "BSCL.Plataforma.BD::PLA.TipoReajuste"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.TipoLog"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.TipoCanalIngreso"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.TablaConversion"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.Sucursal"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.ParametroFiltro"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.Moneda"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.Modulo"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.Filtro"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.Estado"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.EstadoPla"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.Empresa";  
DELETE FROM  "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.Concepto"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.CodigoError"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.Boveda"; 
DELETE FROM  "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz";
DELETE FROM  "BSCL.Plataforma.BD::PLA.TipoRegistro";
DELETE FROM  "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo";
DELETE FROM  "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja";

*/
/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/


INSERT INTO "BSCL.Plataforma.BD::PLA.CamaraDeCompensacion"
VALUES('CCLV','CCLV','CONSTRAPARTE CENTRAL',179630,44,'ACT');



INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" ("IdTipoMovimiento","IdModulo","Tipo","Nombre","IdEstado") 
VALUES ('MOVCAR','CTA','CA','MOVIMIENTO DE CARGO','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" ("IdTipoMovimiento","IdModulo","Tipo","Nombre","IdEstado") 
VALUES ('MOVABN','CTA','AB','MOVIMIENTO DE ABONO','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" ("IdTipoMovimiento","IdModulo","Tipo","Nombre","IdEstado") 
VALUES ('MOVTRN','CTA','TR','MOVIMIENTO DE TRANSFERENCIA','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" ("IdTipoMovimiento","IdModulo","Tipo","Nombre","IdEstado") 
VALUES ('MOVING','TES','IN','MOVIMIENTO DE INGRESO','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoMovimiento" ("IdTipoMovimiento","IdModulo","Tipo","Nombre","IdEstado") 
VALUES ('MOVEGR','TES','EG','MOVIMIENTO DE EGRESO','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoReajuste" ( "IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado" )  
VALUES('CLP','CLP','No Reajustable','N','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoReajuste" ( "IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado" )  
VALUES('USD','USD','Dolar','S','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoReajuste" ( "IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado" )  
VALUES('EUR','EUR','Euro','S','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoReajuste" ( "IdTipoReajuste","Codigo","Nombre","Reajustable","IdEstado" )  
VALUES('UF','UF','Unidad de fomento','S','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.EstadoPla" ("IdEstadoPla","IdEstado","NombreEntidad", "Descripcion") 
VALUES (1,'ACT','MONEDA','ACTIVO');	
INSERT INTO "BSCL.Plataforma.BD::PLA.EstadoPla" ("IdEstadoPla","IdEstado","NombreEntidad", "Descripcion") 
VALUES (2,'INA','MONEDA','INACTIVO');
INSERT INTO "BSCL.Plataforma.BD::PLA.EstadoPla" ("IdEstadoPla","IdEstado","NombreEntidad", "Descripcion") 
VALUES (3,'VIG','MERCADO','VIGENTE');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(4, 'ACT', 'USUARIO', 'ACTIVO');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(5, 'BLO', 'USUARIO', 'BLOQUEADO');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(6, 'ACT', 'ROL', 'ACTIVO');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(7, 'INA', 'ROL', 'INACTIVO');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(8, 'ACT', 'PERFIL', 'ACTIVO');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(9, 'INA', 'PERFIL', 'INACTIVO');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(10, 'ACT', 'GRUPOPORTAFOLIO', 'ACTIVO');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(11, 'INA', 'GRUPOPORTAFOLIO', 'INACTIVO');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(12, 'ACT', 'ARCHIVO', 'ACTIVO');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoPla"("IdEstadoPla", "IdEstado", "NombreEntidad", "Descripcion") 
values(13, 'INA', 'ARCHIVO', 'INACTIVO');
--TipoOperacionYCondicionDeLiquidacion
--COMACN
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(1,'S','T0','COMACN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(2,'S','T1','COMACN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(3,'S','T2','COMACN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(4,'S','T3','COMACN','ACT');
--VENACN
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(5,'S','T0','VENACN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(6,'S','T1','VENACN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(7,'S','T2','VENACN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(8,'S','T3','VENACN','ACT');
--COMACX
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(9,'S','T0','COMACX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(10,'S','T1','COMACX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(11,'S','T2','COMACX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(12,'S','T3','COMACX','ACT');
--VENACX
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(13,'S','T0','VENACX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(14,'S','T1','VENACX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(15,'S','T2','VENACX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(16,'S','T3','VENACX','ACT');
--COMCFI
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(17,'S','T0','COMCFI','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(18,'S','T1','COMCFI','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(19,'S','T2','COMCFI','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(20,'S','T3','COMCFI','ACT');
--VENCFI
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(21,'S','T0','VENCFI','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(22,'S','T1','VENCFI','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(23,'S','T2','VENCFI','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(24,'S','T3','VENCFI','ACT');
--COMCFX
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(25,'S','T0','COMCFX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(26,'S','T1','COMCFX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(27,'S','T2','COMCFX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(28,'S','T3','COMCFX','ACT');
--VENCFX
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(29,'S','T0','VENCFX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(30,'S','T1','VENCFX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(31,'S','T2','VENCFX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(32,'S','T3','VENCFX','ACT');
--COMMON
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(33,'S','T0','COMMON','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(34,'S','T1','COMMON','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(35,'S','T2','COMMON','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(36,'S','T3','COMMON','ACT');
--VENMON
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(37,'S','T0','VENMON','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(38,'S','T1','VENMON','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(39,'S','T2','VENMON','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(40,'S','T3','VENMON','ACT');
--COMETN
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(41,'S','T0','COMETN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(42,'S','T1','COMETN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(43,'S','T2','COMETN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(44,'S','T3','COMETN','ACT');
--VENETN
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(45,'S','T0','VENETN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(46,'S','T1','VENETN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(47,'S','T2','VENETN','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(48,'S','T3','VENETN','ACT');
--COMETX
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(49,'S','T0','COMETX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(50,'S','T1','COMETX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(51,'S','T2','COMETX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(52,'S','T3','COMETX','ACT');
--VENETX
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(53,'S','T0','VENETX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(54,'S','T1','VENETX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(55,'S','T2','VENETX','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(56,'S','T3','VENETX','ACT');
--COMCFM
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(57,'S','T0','COMCFM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(58,'S','T1','COMCFM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(59,'S','T2','COMCFM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(60,'S','T3','COMCFM','ACT');
--VENCFM
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(61,'S','T0','VENCFM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(62,'S','T1','VENCFM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(63,'S','T2','VENCFM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(64,'S','T3','VENCFM','ACT');
--COMSIM
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(65,'S','T0','COMSIM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(66,'S','T1','COMSIM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(67,'S','T2','COMSIM','ACT');
--VENSIM
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(68,'S','T0','VENSIM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(69,'S','T1','VENSIM','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(70,'S','T2','VENSIM','ACT');
--INGTIT
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(71,'S','T0','INGTIT','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(72,'S','T1','INGTIT','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(73,'S','T2','INGTIT','ACT');
--EGRTIT
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(74,'S','T0','EGRTIT','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(75,'S','T1','EGRTIT','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(76,'S','T2','EGRTIT','ACT');
--CARMAN
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(77,'S','T0','CARMAN','ACT');
--ABNMAN
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(78,'S','T0','ABNMAN','ACT');
--CARTRA
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(79,'S','T0','CARTRA','ACT');
--ABNTRA
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(80,'S','T0','ABNTRA','ACT');
--COMRF
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(81,'S','T0','COMRF','ACT');
--VENRF
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(82,'S','T0','VENRF','ACT');
--COMFUT
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(83,'S','T1','COMFUT','ACT');
--VENFUT
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(84,'S','T1','VENFUT','ACT');
--EMPRES
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(85,'S','T0','EMPRES','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(86,'S','T1','EMPRES','ACT');
--PRESTA
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(87,'S','T0','PRESTA','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(88,'S','T1','PRESTA','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(89,'S','T2','PRESTA','ACT');
--COMST
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(90,'S','T0','COMST','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(91,'S','T1','COMST','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(92,'S','T2','COMST','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(96,'S','T3','COMST','ACT');
--VENST
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(93,'S','T0','VENST','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(94,'S','T1','VENST','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(95,'S','T2','VENST','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
VALUES(97,'S','T3','VENST','ACT');
--COMIIF
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T0','COMIIF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
--VENIIF
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T0','VENIIF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
--COMMMIL
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T0','COMMIL','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T1','COMMIL','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T2','COMMIL','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T3','COMMIL','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
--VENMIL
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T0','VENMIL','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T1','VENMIL','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T2','VENMIL','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1, 'S','T3','VENMIL','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
--COMRF
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T1','COMRF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
--VENRF
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T1','VENRF','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
--DIVDIN
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T0','DIVDIN','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";


--COMPAC
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T0','COMPAC','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
--VENPAC
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T0','VENPAC','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";

--COMTER
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T0','COMTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T1','COMTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T2','COMTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T3','COMTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T4','COMTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T5','COMTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";

--VENTER
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T0','VENTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T1','VENTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T2','VENTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T3','VENTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T4','VENTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" ( "IdTipoOperacionYCondicionDeLiquidacion","SoloDiasHabiles","IdCondicionDeLiquidacion","IdTipoOperacion","IdEstado" )  
select max("IdTipoOperacionYCondicionDeLiquidacion")+1,'S','T5','VENTER','ACT' from "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion";



INSERT INTO "BSCL.Plataforma.BD::PLA.TipoLog" ( "IdTipoLog","Descripcion" )  
VALUES('ERROR','ERRORES DE EVENTOS O PROCESOS');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoLog" ( "IdTipoLog","Descripcion" )  
VALUES('INFORMATIVO','REGISTRO DE INFORMACIÓN DE EVENTOS O PROCESOS');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoLog" ( "IdTipoLog","Descripcion" )  
VALUES('MODIFICACION','ERRORES O INFORMATIVO POR MODIFICACIÓN DE DATOS');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoLog" ( "IdTipoLog","Descripcion" )  
VALUES('ELIMINACION','ERRORES O INFORMATIVO POR ELIMINACIÓN DE DATOS ');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoLog" ( "IdTipoLog","Descripcion" )  
VALUES('INICIO','REGISTRO DE INICIO DE PROCESOS');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoLog" ( "IdTipoLog","Descripcion" )  
VALUES('TERMINO','REGISTRO DE TÉRMINO DE PROCESOS');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoLog" ( "IdTipoLog","Descripcion" )  
VALUES('INGRESO','ERRORES O INFORMATIVO POR INGRESO DE DATOS');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoLog" ( "IdTipoLog","Descripcion" )  
VALUES('ANULACION','ERRORES O INFORMATIVO POR ANULACION DE DATOS');

INSERT INTO "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" ( "IdTipoCanalIngreso","Codigo","Nombre","IdEstado" )  
VALUES('AGE','1','AGENTE','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" ( "IdTipoCanalIngreso","Codigo","Nombre","IdEstado" )  
VALUES('CLI','2','CLIENTE','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" ( "IdTipoCanalIngreso","Codigo","Nombre","IdEstado" )  
VALUES('INT','3','INTERNET','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" ( "IdTipoCanalIngreso","Codigo","Nombre","IdEstado" )  
VALUES('TRA','4','TRADER','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" ( "IdTipoCanalIngreso","Codigo","Nombre","IdEstado" )  
VALUES('SIS','5','SISTEMA','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(1,'PLA','TipoEvento','NombreTabla','BSCL.Plataforma.BD::AUT.TipoEvento','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(2,'PLA','TipoEvento','NombreCampoId','IdTipoEvento','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(3,'PLA','TipoInstrumento','NombreTabla','BSCL.Plataforma.BD::INS.TipoInstrumento','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(4,'PLA','TipoInstrumento','NombreCampoId','IdTipoInstrumento','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(5,'PLA','Usuario','NombreTabla','BSCL.Plataforma.BD::ACC.Usuario','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(6,'PLA','Usuario','NombreCampoId','IdUsuario','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(7,'PLA','Rol','NombreTabla','BSCL.Plataforma.BD::ACC.Rol','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(8,'PLA','Rol','NombreCampoId','IdRol','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(9,'PLA','Estado','NombreTabla','BSCL.Plataforma.BD::PLA.Estado','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(10,'PLA','Estado','NombreCampoId','IdEstado','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(11,'PLA','EtapaTipoEvento','NombreTabla','BSCL.Plataforma.BD::AUT.Etapa','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(12,'PLA','EtapaTipoEvento','NombreCampoId','IdEtapa','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(13,'PLA','Modulo','NombreTabla','BSCL.Plataforma.BD::PLA.Modulo','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(14,'PLA','Modulo','NombreCampoId','IdModulo','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(15,'PLA','TipoOperacion','NombreTabla','BSCL.Plataforma.BD::INS.TipoOperacion','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(16,'PLA','TipoOperacion','NombreCampoId','IdTipoOperacion','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(17,'PLA','Concepto','NombreTabla','BSCL.Plataforma.BD::PLA.Concepto','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(18,'PLA','Concepto','NombreCampoId','IdConcepto','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(19,'PLA','EventoAutorizacion','NombreTabla','BSCL.Plataforma.BD::AUT.Evento','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(20,'PLA','EventoAutorizacion','NombreCampoId','IdEvento','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(21,'PLA','EtapaEvento','NombreTabla','BSCL.Plataforma.BD::AUT.EtapaEvento','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(22,'PLA','EtapaEvento','NombreCampoId','IdEtapaEvento','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(23,'PLA','TipoLog','NombreTabla','BSCL.Plataforma.BD::PLA.TipoLog','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(24,'PLA','TipoLog','NombreCampoId','IdTipoLog','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(25,'PLA','Entidad','NombreTabla','BSCL.Plataforma.BD::PLA.Entidad','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(26,'PLA','Entidad','NombreCampoId','IdEntidad','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(27,'CLI','Cliente','NombreTabla','BSCL.Cliente.BD::CLI.Cliente','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(28,'CLI','Cliente','NombreCampoId','IdCliente','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(29,'POR','Portafolio','NombreTabla','BSCL.Portafolio.BD::POR.Portafolio','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(30,'POR','Portafolio','NombreCampoId','IdPortafolio','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(31,'PLA','Cobro','NombreTabla','BSCL.Plataforma.BD::COM.Cobro','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(32,'PLA','Cobro','NombreCampoId','IdCobro','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(33,'PLA','Moneda','NombreTabla','BSCL.Plataforma.BD::PLA.Moneda','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(34,'PLA','Moneda','NombreCampoId','IdMoneda','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(35,'PLA','Pais','NombreTabla','BSCL.Plataforma.BD::REG.Pais','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(36,'PLA','Pais','NombreCampoId','IdPais','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(37,'PLA','CondicionDeLiquidacion','NombreTabla','BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(38,'PLA','CondicionDeLiquidacion','NombreCampoId','IdCondicionDeLiquidacion','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(39,'PLA','TipoProducto','NombreTabla','BSCL.Plataforma.BD::INS.TipoProducto','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(40,'PLA','TipoProducto','NombreCampoId','IdTipoProducto','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(41,'PLA','Instrumento','NombreTabla','BSCL.Plataforma.BD::INS.Instrumento','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.TablaConversion" ( "IdTablaConversion","IdModulo","Codigo","Tipo","Valor","IdEstado" )  
VALUES(42,'PLA','Instrumento','NombreCampoId','IdInstrumento','VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  
VALUES(1,1,'SC01','Sucursal Las Condes','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  
VALUES(2,1,'S002','Sucursal Vitacura','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  
VALUES(3,1,'S003','Sucursal Norte','VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" ) 
VALUES(4,1,'','','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" ) 
VALUES(5,1,'1','Unidad Implemetación','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" ) 
VALUES(6,1,'25','mbi presidente riesco','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" ) 
VALUES(7,1,'1234','Sucursal 1234','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" ) 
VALUES(8,1,'254','base01','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" ) 
VALUES(9,1,'AAUS','SUCURSAL AURRIOLA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" ) 
VALUES(10,1,'becb','Banco Estado CB','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  
VALUES(11,1,'EM01','Empresa 01','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  
VALUES(12,1,'sk','SKANDIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  
VALUES(13,1,'sss','ss','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  
VALUES(14,1,'SU01','LA BOLSA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  
VALUES(15,1,'SU02','LAS CONDES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal" ( "IdSucursal","IdEmpresa","CodigoSucursal","NombreSucursal","EstadoSucursal" )  
VALUES(16,1,'UC01','Inversiones Matte','VIG');




INSERT INTO "BSCL.Plataforma.BD::PLA.ParametroFiltro" ( "IdParametroFitro","IdFiltro","IdServicio","estado" )  
VALUES(1,1,18,'S');

INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" )  
VALUES('CLP','CL',152,'PESO CHILENO','CL',0,'null',0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" )  
VALUES('UF','UF',990,'UNIDAD DE FOMENTO','UF',2,'null',0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" )  
VALUES('USD','DOL',840,'DOLAR','DOL',2,'null',0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" )  
VALUES('EUR','EUR',978,'EURO','EUR',2,'null',0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0,1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" ( "IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","MultiplicaODivide","RangoSuperiorParidad","RangoInferiorParidad","PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio","PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado" )  
VALUES('YEN','YEN',392,'YEN','YEN',2,'null',0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0E-8,0,1,'ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('PLA','PLA','Plataforma','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('TES','TES','Tesoreria','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('CON','CON','Contabilidad','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('CTA','CTA','Cuenta Inversion','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('ORD','ORD','Ordenes','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('CUS','CUS','Custodia','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('TRI','TRI','Tributario','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('CLI','CLI','Cliente','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('MRF','MESRF','Mesa RF','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('MSP','MESSP','Mesa Spot','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('MFW','MESFW','Mesa Forward','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('FAC','FAC','Facturacion','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('SOC','SOC','SocioNegocio','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('LIM','LIM','Limites','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ("IdModulo", "Codigo", "Nombre", "IdEstado")
VALUES ('MFM', 'MESFFMM', 'FONDOS MUTUOS', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo" ( "IdModulo","Codigo","Nombre","IdEstado" )  
VALUES('POR','POR','PORTAFOLIO','ACT');


INSERT INTO "BSCL.Plataforma.BD::PLA.Filtro" ( "IdFiltro","IdRol","valor","spConsulta","operador","descripcion","estado" )  
VALUES(1,1,'IdCliente','POC.ARQ.BD::ConsultarValorParametroDemo','eq','','S');

INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ACT','Activo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ANT','Anticipado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ANU','Anulado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ASI','Asignado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ASC','Completa');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ASP','Parcial');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('SAS','Sin Asignar');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('OFE','Ofertada');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('NEF','No Enviada a Facturar');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('EAF','Enviada a Facturar');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('BLO','Bloqueado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('CAN','Cancelado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('CER','Cerrado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('CNF','Confirmado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ACP','Aceptado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('CRT','Certificado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('EMI','Emitido');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('FAC','Facturado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES ('REF','REFACTURADO');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('INA','Inactivo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ING','Ingresado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('MOD','Modificado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('PEN','Pendiente');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('LIQ','Liquidado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('PLQ','PreLiquidado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('RES','Reservado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('TER','Terminado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('VIG','Vigente');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('VCD','Vencido');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('RZD','Rechazado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('APD','Aprobado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('HAB','Habilitado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ENV','Enviado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('ERR','Error');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('GEN','Generado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('GIR','Girado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('GIP','Girado Parcialmente');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('AUT','Autorizado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('DEP','DEPOSITADO');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES ('LIP','LIQUIDADO PARCIALMENTE');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('EDP','ENVIADO A DEPOSITAR');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado" ( "IdEstado","Nombre" )  
VALUES('NDP','ENVIADO A NOMINA DE DEPOSITO');
INSERT INTO "BSCL.Plataforma.BD::PLA.Estado"("IdEstado","Nombre")
VALUES('SOL', 'SOLICITADO');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Estado"("IdEstado","Nombre")
VALUES('INV','INVALIDO');

/* TABLA BORRADA
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('CLIENTE','CLIENTE');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('PROCCIEMESA','PROCESO CIERRE MESA RF');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('PORTAFOLIO','PORTAFOLIO');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('FACTURA','FACTURA');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('CMBTETRS','COMPROBANTE TRASPASO');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('CMBTEFAC','COMPROBANTE FACTURA');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('MOVIMIENTOCARTERAENTRANSITO','MOVIMIENTO DE CARTERA EN TRANSITO');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('PROCESOCARGA','PROCESO CARGA MASIVA');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('ORDEN','ORDEN');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('MOVCTAINV','MOVIMIENTO DE CUENTA INVERSION');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('CTAINV','CUENTA INVERSION');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('LIMITES','LIMITES');
-- PERFIL COMERCIAL.
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('PERFIL_COMERCIAL','LIMITES');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('PERFIL_PRODUCTOS','LIMITES');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('PERFIL_COBROS','LIMITES');

INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('COBERTURA','COBERTURA DE GARANTIA');

INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('ANTICIPO_SIMULTANEA','ANTICIPO DE SIMULTANEA');
INSERT INTO "BSCL.Plataforma.BD::PLA.Entidad" ( "IdEntidad","Descripcion" )  
VALUES('PROCESO_VENCIMIENTO_SIMULTANEA','PROCESO DE VENCIMIENTO POR PAUTA DE SIMULTANEA');
*/


INSERT INTO "BSCL.Plataforma.BD::PLA.Empresa" ( "IdEmpresa","Codigo","Nombre","IdEstado","IdPais","IdSocioDeNegocio" )  
VALUES(1,'EMP01','HANA Corredora','ACT',44,6449);
INSERT INTO "BSCL.Plataforma.BD::PLA.Empresa" ( "IdEmpresa","Codigo","Nombre","IdEstado","IdPais","IdSocioDeNegocio" )  
VALUES(2,'EMP02','Empresa 2','ACT',44,2);
INSERT INTO "BSCL.Plataforma.BD::PLA.Empresa" ( "IdEmpresa","Codigo","Nombre","IdEstado","IdPais","IdSocioDeNegocio" )  
VALUES(3,'EMP03','Empresa 3','ACT',44,3);

INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ( "IdCondicionDeLiquidacion","Codigo","Nombre","PlazoEnDias" )  
VALUES('T0','PH','Pagadero hoy',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ( "IdCondicionDeLiquidacion","Codigo","Nombre","PlazoEnDias" )  
VALUES('T1','PM','Pagadero mañana',1);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ( "IdCondicionDeLiquidacion","Codigo","Nombre","PlazoEnDias" )  
VALUES('T2','CN','Contado Normal',2);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ( "IdCondicionDeLiquidacion","Codigo","Nombre","PlazoEnDias" )  
VALUES('T3','T3','T3',3);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ( "IdCondicionDeLiquidacion","Codigo","Nombre","PlazoEnDias" )  
VALUES('T4','T4','T4',4);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ( "IdCondicionDeLiquidacion","Codigo","Nombre","PlazoEnDias" )  
VALUES('T5','T5','T5',5);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ( "IdCondicionDeLiquidacion","Codigo","Nombre","PlazoEnDias" )  
VALUES('T6','T6','T6',6);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ( "IdCondicionDeLiquidacion","Codigo","Nombre","PlazoEnDias" )  
VALUES('T7','T7','T7',7);
INSERT INTO "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" ( "IdCondicionDeLiquidacion","Codigo","Nombre","PlazoEnDias" )  
VALUES('TP','TP','Transaccion a Plazo',-1);


/*
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABVTAC','AB','CTA','VENTA ACCIONES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABAPCT','AB','CTA','APORTE CUENTA INVERSION','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTRBC','AB','CTA','TRANSFERENCIA BANCARIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABDVAC','AB','CTA','DIVIDENDO EN ACCIONES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABDVEP','AB','CTA','DIVIDENDO EMISION PAGADA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABREGU','AB','CTA','REGULARIZACIÓN','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABVCCU','AB','CTA','VENCIMIENTO DE CUPÓN','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACPAC','CA','CTA','COMPRA DE ACCIONES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACPFM','CA','CTA','COMPRA FONDOS MUTUOS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACPRF','CA','CTA','COMPRA RENTA FIJA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAREGU','CA','CTA','REGULARIZACIÓN','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CARTCT','CA','CTA','RETIRO CUENTA INVERSION','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ASVTAC','AS','ORD','ASIGNACIÓN DE VENTA ACCIONES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ASCPAC','AS','ORD','ASIGNACIÓN DE COMPRA DE ACCIONES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('INCORV','IN','TES','COMPRA CLIENTE FACTURA RV','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('EGVERV','EG','TES','VENTA CLIENTE FACTURA RV','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('INTIRV','IT','CUS','INGRESO TITULO EN CUSTODIA RV','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('EGTIRV','ET','CUS','EGRESO TITULO EN CUSTODIA RV','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('INTIRF','IT','CUS','INGRESO TITULO EN CUSTODIA RF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('EGTIRF','ET','CUS','EGRESO TITULO EN CUSTODIA RF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('INTIIF','IT','CUS','INGRESO TITULO EN CUSTODIA IF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('EGTIIF','ET','CUS','EGRESO TITULO EN CUSTODIA IF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MOVCAR','CA','CTA','MOVIMIENTO DE CARGO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MOVABN','AB','CTA','MOVIMIENTO DE ABONO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABNVAX','AB','CTA','NETO FACTURA VENTA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACVAX','CA','CTA','COBROS FACTURA VENTA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABNVAN','AB','CTA','NETO FACTURA VENTA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACVAN','CA','CTA','COBROS FACTURA VENTA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAINSP','IN','GTA','GTIA SIN CERTIFICAR POR SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAINSI','IN','GTA','GTIA CERTIFICADA POR SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAINFU','IN','GTA','GTIA POR FUTURO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAINFW','IN','GTA','GTIA POR FORWARD','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAINRF','IN','GTA','GTIA POR RENTA FIJA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAINVC','IN','GTA','GTIA POR VENTA CORTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAINVC','IN','GTA','GTIA POR VENTA CORTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GARTFU','RT','GTA','GTIA POR FUTURO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GARTFW','RT','GTA','GTIA POR FORWARD','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GARTRF','RT','GTA','GTIA POR RENTA FIJA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GARTSI','RT','GTA','GTIA POR SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GARTVC','RT','GTA','GTIA POR VENTA CORTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GARF','GE','GTA','GARANTÍAS RENTA FIJA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAFU','GE','GTA','GARANTÍAS FUTURO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GASI','GE','GTA','GARANTÍAS SIMULTÁNEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAVC','GE','GTA','GARANTÍAS VENTA CORTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('GAFW','GE','GTA','GARANTÍAS FORWARD','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENROLC','EC','CLI','ENROLAMIENTO DE CLIENTE','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTSIN','IN','TES','ANTICIPO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTSEG','EG','TES','ANTICIPO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VCTSIN','IN','TES','VENCIMIENTO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VCTSEG','EG','TES','VENCIMIENTO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTSIT','IT','CUS','ANTICIPO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTSET','ET','CUS','ANTICIPO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VCTSIT','IT','CUS','VENCIMIENTO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VCTSET','ET','CUS','VENCIMIENTO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENROLC','EC','CLI','ENROLAMIENTO DE CLIENTE','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODATR','UP','PLA','MODIFICACION DE ATRIBUTO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('INGPRT','IN','CUS','INGRESO DE PRESTAMO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VENPRT','EG','CUS','VENCIMIENTO DE PRESTAMO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTPRT','EG','CUS','ANTICIPO DE PRESTAMO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('INGEMP','IN','CUS','INGRESO DE EMPRESTITO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VENEMP','EG','CUS','VENCIMIENTO DE EMPRESTITO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTEMP','EG','CUS','ANTICIPO DE EMPRESTITO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FACCOM','FC','FAC','FACTURA DE COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FACVEN','FV','FAC','FACTURA DE VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FAECOM','FC','FAC','FACTURA ELECTRONICA DE COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FAEVEN','FV','FAC','FACTURA ELECTRONICA DE VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCRCOM','NC','FAC','NOTA DE CREDITO COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCRVEN','NV','FAC','NOTA DE CREDITO VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCECOM','NC','FAC','NOTA DE CREDITO ELECTRONICA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCEVEN','NV','FAC','NOTA DE CREDITO ELECTRONICA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDRCOM','NC','FAC','NOTA DE DEBITO DE COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDRVEN','NV','FAC','NOTA DE DEBITO DE VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDECOM','NC','FAC','NOTA DE DEBITO ELECTRONICA DE COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDEVEN','NV','FAC','NOTA DE DEBITO ELECTRONICA DE VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FAOFAO','FO','FAC','FACTURA DE OTROS CONCEPTOS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODPER','MP','SOC','MODIFICACION DE PERSONA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FAOPRI','FO','FAC','FACTURA DE OTROS CONCEPTOS - PRIMA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FAOPRE','FO','FAC','FACTURA DE OTROS CONCEPTOS - PREMIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODDOC','MP','SOC','MODIFICACION DOCUMENTO IDENTIDAD','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANEMIN','IN','CUS','ANULACION DE EMPRESTITO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANEMEG','EG','CUS','ANULACION DE EMPRESTITO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANPRIN','IN','CUS','ANULACION DE PRESTAMO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANPREG','EG','CUS','ANULACION DE PRESTAMO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEDOC','MP','SOC','NUEVO DOCUMENTO IDENTIDAD','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUETEL','MP','SOC','NUEVO TELEFONO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODTEL','MP','SOC','MODIFICACION TELEFONO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEDIF','MP','SOC','NUEVA DIRECCION','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODDIF','MP','SOC','MODIFICACION DIRECCION','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEDIE','MP','SOC','NUEVA DIRECCION ELECTRONICA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODDIE','MP','SOC','MODIFICACION DIRECCION ELECTRONICA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUECTB','MP','SOC','NUEVA CUENTA BANCARIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODCTB','MP','SOC','MODIFICACION CUENTA BANCARIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MCPCOB','PC','PLA','MODIFICACION PERFIL COBRO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MDPCOB','PC','PLA','MODIFICACION DETALLE PERFIL COBRO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODTJB','MP','SOC','MODIFICACION TARJETA BANCARIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUETJB','MP','SOC','NUEVA TARJETA BANCARIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODCLI','MC','SOC','MODIFICACION DE CLIENTE','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAECLI','MC','SOC','CAMBIO DE ESTADO CLIENTE','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAECNT','MC','SOC','CAMBIO DE ESTADO CONTRAPARTE','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAEDOC','MP','SOC','CAMBIO DE ESTADO DOCUMENTO IDENTIDAD','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODEMP','ME','SOC','MODIFICACION DE EMPLEADO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('EMPRES','AB','CTA','EMPRESTITO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('PRESTA','CA','CTA','PRESTAMO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAEEMP','ME','SOC','CAMBIO DE ESTADO EMPLEADO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENROLE','EE','SOC','ENROLAMIENTO DE EMPLEADO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENRCNT','EN','SOC','ENROLAMIENTO DE CONTRAPARTE','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODCNT','MC','SOC','MODIFICACION DE CONTRAPARTE','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENRPRO','EP','SOC','ENROLAMIENTO DE PROVEEDOR','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATCFU','CA','CTA','TOTAL FACTURA COMPRA FUTURO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATVFU','CA','CTA','TOTAL FACTURA VENTA FUTURO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATPRI','CA','CTA','TOTAL FACTURA PRIMA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTVSI','AB','CTA','TOTAL FACTURA VENTA SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATCSI','CA','CTA','TOTAL FACTURA COMPRA SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENROLU','EU','PLA','ENROLAMIENO USUARIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('AROLUS','RU','PLA','AGREGAR ROL USUARIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('DROLUS','RU','PLA','ELIMINAR ROL USUARIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATCRF','CA','CTA','TOTAL FACTURA COMPRA RF','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODDUP','MP','POR','MODIFICACION DATOS USO PORTAFOLIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEDUP','MP','POR','NUEVO DATO USO PORTAFOLIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ENRPER','EP','SOC','ENROLAMIENTO DE PERSONA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTVRF','AB','CTA','TOTAL FACTURA VENTA RF','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATVIF','CA','CTA','TOTAL FACTURA COMPRA IF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTVIF','AB','CTA','TOTAL FACTURA VENTA IF','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTSM','CA','CTA','INGRESO GARANTIA POR SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTFT','CA','CTA','INGRESO GARANTIA POR FUTURO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTFW','CA','CTA','INGRESO GARANTIA POR FORWARD','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTRF','CA','CTA','INGRESO GARANTIA POR RENTA FIJA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('CAGTVC','CA','CTA','INGRESO GARANTIA POR VENTA CORTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTFT','AB','CTA','RETIRO GARANTIA POR FUTURO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTFW','AB','CTA','RETIRO GARANTIA POR FORWARD','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTRF','AB','CTA','RETIRO GARANTIA POR RENTA FIJA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTVC','AB','CTA','RETIRO GARANTIA POR VENTA CORTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('ABGTSM','AB','CTA','RETIRO GARANTIA POR SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAEGTE','CA','CTA','COMPROBANTE EGRESO TESORERIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABIGTE','AB','CTA','COMPROBANTE INGRESO TESORERIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABVCTS','AB','CTA','VENCIMIENTO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAVCTS','AB','CTA','VENCIMIENTO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CAANSV','CA','CTA','ANTICIPO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('ABANSC','AB','CTA','ANTICIPO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTPRE','AB','CTA','TOTAL FACTURA PREMIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACCAX','CA','CTA','COBROS FACTURA COMPRA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CACCAN','CA','CTA','COBROS FACTURA COMPRA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CANCAX','CA','CTA','NETO FACTURA COMPRA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CANCAN','CA','CTA','NETO FACTURA COMPRA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATCAX','CA','CTA','TOTAL FACTURA COMPRA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CATCAN','CA','CTA','TOTAL FACTURA COMPRA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTVAX','AB','CTA','TOTAL FACTURA VENTA ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABTVAN','AB','CTA','TOTAL FACTURA VENTA ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('MOVTRN','TR','CTA','MOVIMIENTO DE TRANSFERENCIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('TRTRAN','TR','CTA','TRANSFERENCIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('ANTIRV','','CUS','ANULACION TITULO EN CUSTODIA RV','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('NUENFA','','SOC','NUEVO NOMBRE FANTASIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CAENFA','','SOC','CAMBIO ESTADO NOMBRE FANTASIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CAEPOR','','SOC','CAMBIO ESTADO PORTAFOLIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CAEPER','','PER','CAMBIO DE ESTADO PERSONA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEPOR','MP','SOC','NUEVO PORTAFOLIO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODPOR','MP','SOC','MODIFICACION DE PORTAFOLIO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUGDIE','MP','SOC','NUEVO GRUPO DIRECCIONES','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MOGDIE','MP','SOC','MODIFICACION DE GRUPO DIRECCIONES','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEREL','MP','SOC','NUEVA RELACION DE PERSONAS','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODREL','MP','SOC','MODIFICACION RELACION DE PERSONAS','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NAUTOR','MP','SOC','NUEVO AUTORIZADO A DAR ORDENES','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('CAEAUT','MP','SOC','CAMBIO DE ESTADO AUTORIZADO A DAR ORDENES','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDPCOB','PC','PLA','NUEVO DETALLE PERFIL COBRO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCPCOB','PC','PLA','NUEVO PERFIL COBRO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDPPRO','PC','PLA','NUEVO DETALLE PERFIL PRODUCTO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCPPRO','PC','PLA','NUEVO PERFIL PRODUCTO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MDPPRO','PC','PLA','MODIFICACION DETALLE PERFIL PRODUCTO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MCPPRO','PC','PLA','MODIFICACION PERFIL PRODUCTO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCPPAG','PC','PLA','NUEVA PREFERENCIA DE PAGO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MCPPAG','PC','PLA','MODIFICACION PREFERENCIA DE PAGO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODIND','MC','PLA','MODIFICACION DE INDICADOR','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUEDOF','','SOC','NUEVA DECLARACIÓN DE ORIGEN DE FONDOS','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODDOF','','SOC','MODIFICACIÓN DECLARACIÓN DE ORIGEN DE FONDOS','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('INCLC0','','TES','INGRESO POR PAGO DE CCLV CAMARA 0 (RV)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('EGCLC0','','TES','EGRESO POR PAGO A CCLV CAMARA 0 (RV)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('INCLC1','','TES','INGRESO POR PAGO DE CCLV CAMARA 1 (PM)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('EGCLC1','','TES','EGRESO POR PAGO A CCLV CAMARA 1 (PM)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('INCLC2','','TES','INGRESO POR PAGO DE CCLV CAMARA 2 (PH+SM)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('EGCLC2','','TES','EGRESO POR PAGO A CCLV CAMARA 2 (PH+SM)','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('APERMO','','PLA','APERTURA DE MODULO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('CIERMO','','PLA','CIERRE DE MODULO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FACMSM','','FAC','FACTURA COMPRA DE SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FAVTSM','','FAC','FACTURA VENTA DE SIMULTANEA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FACMAC','','FAC','FACTURA COMPRA DE ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FAPRES','','FAC','FACTURA DE PRESTAMO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FAEMPR','','FAC','FACTURA DE EMPRESTITO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FAVNAC','','FAC','FACTURA VENTA DE ACCIONES NACIONALES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VCTSIC','IT','CUS','VENCIMIENTO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('VCTSIV','ET','CUS','VENCIMIENTO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTSIC','IT','CUS','ANTICIPO SIMULTANEA COMPRA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANTSIV','ET','CUS','ANTICIPO SIMULTANEA VENTA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEPRES','','FAC','FACTURA ELECTRONICA DE PRESTAMO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FOPRES','','FAC','FACTURA OTROS CONCEPTOS DE PRESTAMO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMAX','','FAC','FACTURA ELECTRONICA DE COMPRA DE ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVNAX','','FAC','FACTURA ELECTRONICA DE VENTA DE ACCIONES EXTRANJERAS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMMI','','FAC','FACTURA ELECTRONICA DE COMPRA DE ACCIONES MILA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVNMI','','FAC','FACTURA ELECTRONICA DE VENTA DE ACCIONES MILA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMRF','','FAC','FACTURA ELECTRONICA DE COMPRA DE RF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVNRF','','FAC','FACTURA ELECTRONICA DE VENTA DE RF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FECMIF','','FAC','FACTURA ELECTRONICA DE COMPRA DE IF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('FEVNIF','','FAC','FACTURA ELECTRONICA DE VENTA DE IF','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES ('ABDIMO','AB','CTA','ABONO DIVIDENDO EN DINERO','VIG')
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODFAT','MP','SOC','MODIFICACION FATCA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ABDIMT','AB','TES','DIVIDENDO EN DINERO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NCPLIM','PC','PLA','NUEVA PLANTILLA LIMITES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MCPLIM','PC','PLA','MODIFICACION PLANTILLA LIMITES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NDPLIM','PC','PLA','NUEVO DETALLE PLANTILLA LIMITES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MDPLIM','PC','PLA','MODIFICACION DETALLE PLANTILLA LIMITES','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NUELIM','PC','PLA','NUEVO LIMITE','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MODLIM','PC','PLA','MODIFICACION LIMITE','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIPPRO','LI','PLA','EVALUACION LIMITE PRODUCTO PORTAFOLIO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIPSGR','LI','PLA','EVALUACION LIMITE SOBREGIRO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NLICTP','LI','PLA','NUEVO LIMITE CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MLICTP','LI','PLA','MODIFICACIÓN LIMITE CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NELCTP','LI','PLA','NUEVO EMISOR LIMITE CUENTA PROPIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MELCTP','LI','PLA','MODIFICACION EMISOR LIMITE CUENTA PROPIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIEMCP','LI','PLA','EVALUACION LIMITE EMISOR CUENTA PROPIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LICRCP','LI','PLA','EVALUACION LIMITE CLAS. RIESGO CUENTA PROPIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )
VALUES('NRLCTP','LI','PLA','NUEVO RANGO LIMITE CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MRLCTP','LI','PLA','MODIFICACION RANGO LIMITE CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIMTCP','LI','PLA','EVALUACION LIMITE MARGEN DE TASA CUENTA PROPIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIPLCP','LI','PLA','EVALUACION LIMITE PLAZO CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('COLICP','LI','PLA','COPIAR LÍMITE CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LISICP','LI','PLA','EVALUACION LIMITE POSICION INTERBANCARIA CUENTA PROPIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LISDCP','LI','PLA','EVALUACION LIMITE MESA DISTRIBUCION CUENTA PROPIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIFDCP','LI','PLA','EVALUACION LIMITE FORWARD CUENTA PROPIA','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LICACP','LI','PLA','EVALUACION LIMITE CARTERA CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('LIAGCP','LI','PLA','EVALUACION LIMITE AGENTE CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NGELCP','LI','PLA','NUEVO GRUPO EMISOR LÍMITE CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MGELCP','LI','PLA','MODIFICACIÓN GRUPO EMISOR LÍMITE CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NEDLCP','LI','PLA','NUEVO GRUPO EMISOR DETALLE LÍMITE CUENTA PROPIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MEDLCP','LI','PLA','MODIFICACIÓN GRUPO EMISOR DETALLE LÍMITE CUENTA PROPIA','VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('ANCOTE','TA','TES','ANULACION COMPROBANTE TESORERIA LIQUIDADO','VIG'); 
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('FOANEM','','FAC', 'FACTURA OTROS CONCEPTOS - ANTICIPO EMPRESTITO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('FOVEEM','','FAC', 'FACTURA OTROS CONCEPTOS - VENCIMIENTO EMPRESTITO','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" ) 
VALUES('FOPRIM','','FAC', 'FACTURA OTROS CONCEPTOS - PRIMA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "Tipo", "IdModulo", "Nombre", "IdEstado")
VALUES('FOANPR', '', 'FAC', 'FACTURA OTROS CONCEPTOS - ANTICIPO PRESTAMO', 'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "Tipo", "IdModulo", "Nombre", "IdEstado")
VALUES('VVISTA','ST', 'TES', 'EFECTIVO', 'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "Tipo", "IdModulo", "Nombre", "IdEstado")
VALUES('EFTIVO','ST', 'TES', 'EFECTIVO', 'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "Tipo", "IdModulo", "Nombre", "IdEstado")
VALUES('MIBNCO','ST', 'TES', 'MISMO BANCO', 'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "Tipo", "IdModulo", "Nombre", "IdEstado")
VALUES('OTBNCO','ST', 'TES', 'OTRO BANCO', 'VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FECMCX','','FAC','FACTURA ELECTRONICA DE COMPRA DE CUOTAS DE FONDOS DE INVERSION EXTRANJEROS','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('FEVNCX','','FAC','FACTURA ELECTRONICA DE VENTA DE CUOTAS DE FONDOS DE INVERSION EXTRANJEROS','VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES ('ABCUMT', 'AB','TES','CORTE CUPON', 'VIG');

INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" 
VALUES ('CORCUP', 'CORCUP', 'CORTE CUPON', 'ABONO', 'EVCA', 'N', 'N', 'N', 'N', 'N', 'INGRESO', '', '', 'R', 'N', 'N', 'N');


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
SELECT 'ITCTER','IT','MES','INGRESO TITULO EN CUSTODIA COMPRA A TERMINO','VIG' FROM DUMMY UNION ALL
SELECT 'RTVTER','RT','MES','RETIRO TITULO EN CUSTODIA VENTA A TERMINO','VIG' FROM DUMMY UNION ALL
SELECT 'ITCPAC','IT','MES','INGRESO TITULO EN CUSTODIA COMPRA PACTO','VIG' FROM DUMMY UNION ALL
SELECT 'RTVPAC','RT','MES','RETIRO TITULO EN CUSTODIA VENTA PACTO','VIG' FROM DUMMY;


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('NPCCUS','','SOC','NUEVA CUENTA DE CUSTODIO PORTAFOLIO','VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","Tipo","IdModulo","Nombre","IdEstado" )  
VALUES('MPCCUS','','SOC','MODIFICACION CUENTA DE CUSTODIO PORTAFOLIO','VIG');

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" )  
VALUES('SORLET','CUS','SORTEO DE LETRAS','VIG');


--CONCEPTOS MIGRACION
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABVFWC','AB','FWD','ABONO POR VCTO. DE FORWARD COMPENSADO ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABSOLT','AB','CUS','ABONO POR SORTEO LETRAS ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABVAFU','AB','CUS','ABONO POR VALORIZACION FUTURO ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABVEFU','CA','CUS','ABONO POR VENCIMIENTO FUTURO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABVEPC','AB','MES','ABONO VENTA RETROC IF-RF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABVEFR','AB','MES','ABONO VENTA FR IF-RF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABCOPC','AB','MES','ABONO RETROV IF-RF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAVFWC','CA','FWD','CARGO POR VCTO DE FWD COMPENSADO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CADISL','CA','CUS','CARGO EN DINERO POR SORTEO LETRAS ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAVAFU','CA','CUS','CARGO POR VALORIZACION FUTURO ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAVEFU','CA','CUS','CARGO POR VENCIMIENTO FUTURO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CACOPC','CA','MES','CARGO COMPRA RETROV IF-RF ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CADEPL','CA','CTA','CARGO DEPOSITO A PLAZO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CACOFR','CA','MES','CARGO COMPRA FR IF-RF ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CALIPL','CA','TES','LIQUIDACION COMPRA TP ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABPACU','AB','TES','PAGO DE CUPONES','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CADEPL','CA','CTA','CARGO DE DEPOSITO A PLAZO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAVEPC','CA','MES','CARGO RETROC IF-RF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAPCUC','CA','TES','PAGO CUPONES EN CONTRA','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABLIPL','AB','TES','LIQUIDACION VENTA TP','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABODIV','AB','CUS','DIVIDENDO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CADISL','CA','CUS','DINERO POR SORTEO LETRAS','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAEMPA','CA','CUS','EMISION PAGADA','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAFCEX','CA','FAC','FACTURA DE COMPRA EXENTA OTROS CONCEPTOS ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAFCFU','CA','FAC','FACTURA DE COMPRA DE FUTUROS','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAFCIF','CA','FAC','FACTURA COMPRA IF ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAFCOT','CA','FAC','FACTURA COMPRA OTROS','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAFCRF','CA','FAC','FACTURA COMPRA RF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAFCRV','CA','FAC','FACTURA COMPRA RV ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAFCPL','CA','FAC','FACTURA COMPRA PLAZO ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABFCVO','AB','FAC','FACTURAS DE VENTAS EXENTAS OT. CONCEPTOS','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAFVFU','CA','FAC','FACTURA DE VENTA DE FUTUROS','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABFVIF','AB','FAC','FACTURA VENTA IF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABFVOC','AB','FAC','FACTURA VENTA OTROS','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABFVRF','AB','FAC','FACTURA VENTA IRF EN EUR','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABFVRV','AB','FAC','FACTURA VENTA RV','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABFVPL','AB','FAC','FACTURA VENTA PLAZO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABDIVC','AB','CUS','ABONO DIVIDENDO VENTA CORTA ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABDIPR','AB','CUS','ABONO DIVIDENDO PRESTAMISTA ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAMOCO','CA','FAC','FACTURA MONTO DE COBRO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABNCPL','AB','FAC','NOTA CREDITO COMPRA PLAZO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CANVPL','CA','FAC','NOTA CREDITO VENTA PLAZO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABNCOC','AB','FAC','NOTA DE CREDITO COMPRA OTROS CONCEPTOS','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABNCIF','AB','FAC','NOTA CREDITO COMPRA IF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABNCRF','AB','FAC','NOTA CREDITO COMPRA RF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABNCRV','AB','FAC','NOTA CREDITO COMPRA RV','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABNCFU','AB','FAC','NOTA CREDITO COMPRA FUTUROS','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABNVFU','AB','FAC','NOTA CREDITO VENTA FUTUROS','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CANVRF','CA','FAC','NOTA CREDITO VENTA RF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CANVRV','CA','FAC','NOTA CREDITO VENTA RV','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABNVPL','AB','FAC','NOTA CREDITO VENTA PLAZO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABNVOC','AB','FAC','NOTA DE CREDITO VENTA OTROS CONCEPTOS ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CANVIF','CA','FAC','NOTA CREDITO VENTA IF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CARESO','CA','TES','REPARTO CON SOBREGIRO ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CADICO','CA','CUS','DIVIDENDO EN CONTRA','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABPRPE','AB','ORD','INGRESO DE PRÉSTAMO POR PRESTAMISTA ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABLAPR','AB','TES','LIQUIDACIÓN ANTICIPADA PRESTAMISTA','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CALPRV','CA','TES','LIQUIDACIÓN PRESTAMISTA AL VENCIMIENTO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABREDI','AB','TES','REPARTO DE DINERO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABRGTA','AB','CUS','RETIRO GARANTIA','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAFMAP','CA','CTA','APORTE FONDO MUTUO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABREFM','AB','CTA','RETIRO FONDO MUTUO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABVEIF','AB','CUS','VENCIMIENTO IF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABINVC','AB','ORD','INGRESO VENTA CORTA','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CALIPR','AB','ORD','LIQUIDACIÓN ANTICIPADA DE PRÉSTAMO','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CALPVE','CA','ORD','LIQUIDACIÓN PRÉSTAMO AL VENCIMIENTO ','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABVERF','AB','CUS','VENCIMIENTO DIARIO DE INSTRUMENTO RF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABLDPD','AB','CTA','ABONO POR LIQUIDACION DE DPF','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('CAMIGR','CA','CTA','CARGO POR INICIAL POR MIGRACION','VIG');
insert into "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") values ('ABMIGR','AB','CTA','ABONO POR INICIAL POR MIGRACION','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","Tipo","IdModulo","Nombre","IdEstado") VALUES ('FECOSA','CA','FAC','FACTURA ELECTRONICA SUSCRIPCION DE OSA','VIG');
*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" ) VALUES('CAMIGR','CTA','CARGO INICIAL POR MIGRACION','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" ) VALUES('ABMIGR','CTA','ABONO INICIAL POR MIGRACION','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" ) VALUES('TRTRAN','CTA','TRANSFERENCIA','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" ) VALUES('LQSDCI','CTA','LIQUIDACION DE SALDOS CUENTA INVERSION','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" ) VALUES('APORCI','CTA','APORTE CUENTA INVERSION','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" ) VALUES('RTROCI','CTA','RETIRO CUENTA INVERSION','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" ) VALUES('RGLRZN','CTA','REGULARIZACIÓN','VIG');


INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" )  
SELECT 'CPTRME','MES','COMPRA A TERMINO MESA RF/IF','VIG' FROM DUMMY UNION ALL
SELECT 'VNTRME','MES','VENTA A TERMINO MESA RF/IF','VIG' FROM DUMMY UNION ALL
SELECT 'CPPCME','MES','COMPRA CON PACTO MESA RF/IF','VIG' FROM DUMMY UNION ALL
SELECT 'VNPCME','MES','VENTA CON PACTO MESA RF/IF','VIG' FROM DUMMY UNION ALL
SELECT 'INGRES','CUS','INGRESO RESERVA','VIG' FROM DUMMY UNION ALL
SELECT 'RETRES','CUS','RETIRO RESERVA','VIG' FROM DUMMY;


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","IdModulo","Nombre","IdEstado")
VALUES('REPRSE' ,'TES','RECAUDACION PROVEEDOR SERVIPAG','VIG');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Concepto"("IdConcepto","IdModulo","Nombre","IdEstado")
VALUES('LIQBIL','TES','LIQUIDACION BILATERAL','VIG');
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","IdModulo", "Nombre", "IdEstado")
VALUES('SAINCT','TES','SALDO INICIAL CUENTA CORRIENTE','VIG');



-- CGO
INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('CIECSU', 'CUS','CIERRE SUSCRIPCION', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('CIECCA', 'CUS','CANJE DE ACCIONES', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('ACECEP', 'CUS','ACTUALZACION SUSCRIPCION', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('CIECDV', 'CUS','CIERRE DIVIDENDOS DINERO', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('CIECSL', 'CUS','CIERRE SORTEO LETRAS', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('CIECCC', 'CUS','CIERRE CORTE CUPON', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('CIECDA', 'CUS','CIERRE DIVIDENDOS ACCIONES', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('SUECOS', 'CUS','SUSCRIPCION EVENTO CAPITAL OSA', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "Nombre", "IdEstado")
VALUES ('DIEVAC', 'DIVIDENDO EN ACCIONES', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('CIECDS', 'CUS','CIERRE DIVISION DE SOCIEDAD', 'VIG');


INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('CLEFIN', 'TES','CLEARING FINANCIERO', 'VIG');
--
-- CGO

INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10001,'RUT_INVALIDO');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10002,'REGLA_FALTA_PARAMETRO');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10003,'FECHA_DE_SISTEMA_NO_CONFIGURADA');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10004,'ESTADO_NO_CONFIGURADO');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10005,'CODIGO_NO_EXISTE');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10006,'FECHA_LOCAL_SIN_DATOS');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10007,'PARAMETRO_DE_CONFIGURACION_NO_EXISTE');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10008,'COND_LIQUIDACION_TIPO_OPER_NO_PERTENECE');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10009,'EMPRESA_NO_EXISTE');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10010,'SOCIO_NEGOCIO_NO_EXISTE');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10011,'COBRO_MAL_DEFINIDO');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10012,'TIME_OUT');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10013,'VALORIZADOR');
INSERT INTO "BSCL.Plataforma.BD::PLA.CodigoError" ( "IdError","Codigo" )  
VALUES(10014,'EVENTO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Boveda" ( "IdBoveda","Codigo","Nombre","IdEstado" )  
VALUES('DCV','DCV','Deposito Central de Valores','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Boveda" ( "IdBoveda","Codigo","Nombre","IdEstado" )  
VALUES('COM','COM','Compania','VIG');
INSERT INTO "BSCL.Plataforma.BD::PLA.Boveda" ( "IdBoveda","Codigo","Nombre","IdEstado" )  
VALUES('COR','COR','Corredora','VIG');



INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" ("IdParidadYTipoCambio" ,"Fecha","IdMoneda","IdUsuarioIngreso","TipoCambioMercado","ParidadMercado","IdEstado") 
VALUES (1,'2015-09-18','CLP',1,1,670,'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" ("IdParidadYTipoCambio" ,"Fecha","IdMoneda","IdUsuarioIngreso","TipoCambioMercado","ParidadMercado","IdEstado") 
VALUES (2,'2015-09-18','USD',1,670,1,'ACT');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (1,'IngresarTituloCtrl','ComboBox','combobox-clasificacion-tipo-instrumento','ACT','
{
    "logic": "or",
    "filters": [
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"}
    ]
}');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (2,'IngresarTituloCtrl','ComboBox','combobox-operacion','ACT','
{
    "logic": "or",
    "filters": [
    {"field": "IdTipoOperacion","operator": "eq","value": "INGTIT"},
    {"field": "IdTipoOperacion","operator": "eq","value": "EGRTIT"},
    {"field": "IdTipoOperacion","operator": "eq","value": "TRATIT"}
    ]
}');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (3,'IngresarTituloCtrl','ComboBox','combobox-tipo-instrumento','ACT','
{
    "logic": "and",
    "filters": [
    {"field": "IdTipoInstrumento","operator": "neq","value": "SIM"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PAC"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PRT"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "EMP"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "MON"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "FRW"}
    ]
}');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (4,'ConsultarMovimientosEnTransitoCtrl','ComboBox','combobox-tipo-instrumento','ACT','
{
    "logic": "and",
    "filters": [
    {"field": "IdTipoInstrumento","operator": "neq","value": "SIM"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PAC"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PRT"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "EMP"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "MON"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "FRW"}
    ]
}');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (5,'ConsultarMovimientosEnTransitoCtrl','ComboBox','combobox-clasificacion-tipo-instrumento','ACT','
{
    "logic": "or",
    "filters": [
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"}
    ]
}');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (6,'ConsultarMovimientosEnTransitoHistoricoCtrl','ComboBox','combobox-clasificacion-tipo-instrumento','ACT','
{
    "logic": "or",
    "filters": [
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"}
    ]
}');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
("IdConfiguracionFiltroInterfaz","Controlador","TipoObjeto","NombreObjeto","IdEstado","Filtros") 
VALUES (7,'ConsultarMovimientosEnTransitoHistoricoCtrl','ComboBox','combobox-tipo-instrumento','ACT','
{
    "logic": "and",
    "filters": [
    {"field": "IdTipoInstrumento","operator": "neq","value": "SIM"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PAC"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "PRT"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "EMP"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "MON"},
    {"field": "IdTipoInstrumento","operator": "neq","value": "FRW"}
    ]
}');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" 
SELECT 8,'IngresoNominalesMesaRFCtrl','ComboBox','combobox-clasificacion-tipo-instrumento','ACT',
'{
    "logic": "or",
    "filters": [
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
    {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"}
    ]
}'
FROM DUMMY;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoTipoAtributo" values(1,'ACT','ATR.TipoAtributo');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.EstadoTipoAtributo" values(2,'INA','ATR.TipoAtributo');


insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('ORDEN', 'ORDEN');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('TRANSACCION', 'TRANSACCION');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('ASIGNACION', 'ASIGNACION');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CLIENTE', 'CLIENTE');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('PORTAFOLIO', 'PORTAFOLIO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('PERSONA', 'PERSONA');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('EMPLEADO', 'EMPLEADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CONTRAPARTE', 'CONTRAPARTE');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CLIENTE-ESTADO', 'CLIENTE-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CONTRAPARTE-ESTADO', 'CONTRAPARTE-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('EMPLEADO-ESTADO', 'EMPLEADO-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('PERSONA-ESTADO', 'PERSONA-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('PROVEEDOR-ESTADO', 'PROVEEDOR-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CUENTABANCARIA-ESTADO', 'CUENTABANCARIA-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('DOCUMENTOIDENTIDAD-ESTADO', 'DOCUMENTOIDENTIDAD-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('NOMBREFANTASIA-ESTADO', 'NOMBREFANTASIA-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('TARJETABANCARIA-ESTADO', 'TARJETABANCARIA-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('ATRIBUTO', 'ATRIBUTO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('ORDENAUTORIZADO-ESTADO', 'ORDENAUTORIZADO-ESTADO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('DIRECCIONELECTRONICA', 'DIRECCIONELECTRONICA');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('DIRECCIONFISICA', 'DIRECCIONFISICA');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('DOCUMENTOIDENTIDAD', 'DOCUMENTOIDENTIDAD');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('TARJETABANCARIA', 'TARJETABANCARIA');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('TELEFONO', 'TELEFONO');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('CARTERA', 'CARTERA');
insert into "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") values('GARANTIA', 'GARANTIA');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") VALUES('MOVIMIENTOCARTERAENTRANSITO', 'MOVIMIENTO DE CARTERA EN TRANSITO');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") VALUES('CLIENTEDOF', 'DECLARACIÓN DE ORIGEN DE FONDOS');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoRegistro" ("IdTipoRegistro", "Descripcion") VALUES('SESION', 'SESION');

INSERT INTO "BSCL.Plataforma.BD::PLA.CRETipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado") VALUES ('USU', 'Usuario', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.CRETipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado") VALUES ('PRF', 'Perfil', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.CRETipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado") VALUES ('PRO', 'Procesos y Funciones', 'ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.CRETipoRequisito" ("IdTipoRequisito", "Nombre", "IdEstado") VALUES ('HOR', 'Horario', 'ACT');


INSERT INTO "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" ( "IdTipoDestinatario","Nombre","IdEstado")  
VALUES('PARA','Para','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" ( "IdTipoDestinatario","Nombre","IdEstado")  
VALUES('CC','Copia','ACT');
INSERT INTO "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" ( "IdTipoDestinatario","Nombre","IdEstado")  
VALUES('CCO','Copia Oculta','ACT');



insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MCPCOB','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MDPCOB','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NCPCOB','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NDPCOB','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NCPPRO','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NDPPRO','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MCPPRO','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MDPPRO','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NCGACT','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NDGACT','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MCGACT','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MDGACT','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUGDIE','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MOGDIE','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODCLI','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODCNT','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAECNT','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODEMP','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAEEMP','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODCTB','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUECTB','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAEPER','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODPER','S');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDIE','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDIE','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDIF','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDIF','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDOC','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDOC','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDID','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUENFA','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAENFA','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODTJB','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUETJB','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODTEL','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUETEL','N');                   
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEREL','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODREL','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAEPRO','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODPRO','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NAUTOR','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAEPOR','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEPOR','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODPOR','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDUP','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDUP','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENROLC','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENRCNT','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENROLE','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENRPER','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENRPRO','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAECLI','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NCPPAG','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MCPPAG','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('CAEAUT','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODIND','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NCPLIM','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MCPLIM','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NDPLIM','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MDPLIM','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUELIM','N');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODLIM','N');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") VALUES('ANCOTE','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LIPPRO','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LIPSGR','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NLICTP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MLICTP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NELCTP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MELCTP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LIEMCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NUEDOF','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MODDOF','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LICRCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NRLCTP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MRLCTP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LIMTCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LIPLCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('COLICP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LISICP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LISDCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LIFDCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LICACP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('LIAGCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NGELCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MGELCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NEDLCP','S');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MEDLCP','S');

insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('AROLUS','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('DROLUS','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('ENROLU','S');  

insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('NPCCUS','S');  
insert into "BSCL"."BSCL.Plataforma.BD::PLA.RequiereAutorizacion"("IdConcepto","RequiereAutorizacion") values('MPCCUS','S');  


--Flujo de Caja
DELETE FROM "BSCL.Plataforma.BD::PLA.UsoFlujoCaja";
GO

INSERT INTO "BSCL.Plataforma.BD::PLA.UsoFlujoCaja"
SELECT 1,'ORDENES_DE_COMPRA','USO PARA EL INGRESO DE ORDENES DE COMPRA','S',1,'ACT' FROM DUMMY UNION ALL
SELECT 2,'CUENTA_INVERSION','INGRESO MANUAL DE CARGOS Y ABONOS','S',1,'ACT' FROM DUMMY UNION ALL
SELECT 3,'INGRESO_APORTE_FMM','INGRESO DE APORTES DE FONDOS MUTUOS','S',1,'ACT' FROM DUMMY UNION ALL
SELECT 4,'CERTIFICACION_APORTE_FMM','CERTIFICACION DE APORTES DE FONDOS MUTUOS','S',1,'ACT' FROM DUMMY UNION ALL
SELECT 5,'CUENTA_INVERSION','MOVIMIENTOS MANUALES DE CUENTA INVERSION','S',1,'ACT' FROM DUMMY UNION ALL
SELECT 6,'MESA_RF','USO EN INGRESO DE OPERACIONES MESA RF','S',1,'ACT' FROM DUMMY UNION ALL
SELECT 100000,'PRUEBA_FLUJO_CAJA','PRUEBAS INTERNAS DEL FLUJO CAJA','N',1,'ACT' FROM DUMMY;
GO


DELETE FROM "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja";
GO

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
SELECT 1,'MOVIMIENTOS_FACTURAR_COMPRAS','FACTURACION','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 1,'ORDENES_EN_TRANSITO_COMPRAS','ORDEN','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 1,'ASIGNACIONES_COMPRAS','ASIGNACION','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 1,'APORTES_EN_TRANSITO','FONDOS_MUTUOS','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 1,'INGRESOS_TESORERIA','TESORERIA','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 1,'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 1,'COMPRA_PACTO','PACTOS','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 1,'COMPRA_TERMINO','TERMINO','','-','ACT','N' FROM DUMMY UNION ALL
--
SELECT 3,'ORDENES_EN_TRANSITO_COMPRAS','ORDEN','Ordenes de compra','-','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'ORDENES_EN_TRANSITO_VENTAS','ORDEN','Ordenes de venta','+','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'ASIGNACIONES_COMPRAS','ASIGNACION','Asignaciones de compra','-','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'ASIGNACIONES_VENTAS','ASIGNACION','Asignaciones de venta','+','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'APORTES_EN_TRANSITO','FONDOS_MUTUOS','Aportes ingresados','-','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA','FONDOS_MUTUOS','Aportes ingresados con Instrucción Liquidada','-','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'RESCATES_EN_TRANSITO','FONDOS_MUTUOS','Rescates negociados (*a fecha pago T1)','+','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'INGRESOS_TESORERIA','TESORERIA','Ingresos ingresados','+','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'EGRESOS_TESORERIA','TESORERIA','Egresos ingresados','-','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','saldo de la cta de inv','+','ACT','N' FROM DUMMY UNION ALL
SELECT 3,'INVERSION_EN_LIQUIDEZ','FONDOS_MUTUOS','Inversion en Liquidez (MoneyMarket)','+','ACT','N' FROM DUMMY UNION ALL
--
SELECT 4,'APORTES_EN_TRANSITO','FONDOS_MUTUOS','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 4,'RESCATES_EN_TRANSITO','FONDOS_MUTUOS','Rescates negociados (*a fecha pago T1)','-','ACT','N' FROM DUMMY UNION ALL
SELECT 4,'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','','+','ACT','N' FROM DUMMY UNION ALL
--
SELECT 5,'ORDENES_EN_TRANSITO_COMPRAS','ORDEN','Ordenes de compra','-','ACT','N' FROM DUMMY UNION ALL
SELECT 5,'ORDENES_EN_TRANSITO_VENTAS','ORDEN','Ordenes de venta','+','ACT','N' FROM DUMMY UNION ALL
SELECT 5,'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','','+','ACT','N' FROM DUMMY UNION ALL
--
SELECT 6,'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 6,'COMPRA_PACTO','PACTOS','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 6,'COMPRA_TERMINO','TERMINO','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 6,'VENTA_PACTO','PACTOS','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 6,'VENTA_TERMINO','TERMINO','','-','ACT','N' FROM DUMMY UNION ALL
--
SELECT 100000,'DIVIDENDOS_EN_DINERO','EVENTO_CAPITAL','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'CORTE_CUPON','EVENTO_CAPITAL','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'SORTEO_DE_LETRAS','EVENTO_CAPITAL','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'MOVIMIENTOS_FACTURAR_VENTAS','FACTURACION','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'MOVIMIENTOS_FACTURAR_COMPRAS','FACTURACION','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'ORDENES_EN_TRANSITO_COMPRAS','ORDEN','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'ORDENES_EN_TRANSITO_VENTAS','ORDEN','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'ASIGNACIONES_COMPRAS','ASIGNACION','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'ASIGNACIONES_VENTAS','ASIGNACION','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'SIMULTANEA_COMPRAS','SIMULTANEA','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'SIMULTANEA_VENTAS','SIMULTANEA','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'APORTES_EN_TRANSITO','FONDOS_MUTUOS','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'RESCATES_EN_TRANSITO','FONDOS_MUTUOS','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'INGRESOS_TESORERIA','TESORERIA','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'EGRESOS_TESORERIA','TESORERIA','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'EMPRESTITOS','PRESTAMO','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'PRESTAMOS','PRESTAMO','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'CARGOS_PENDIENTES_CUENTA_INVERSION','CUENTA_INVERSION','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'COMPRA_PACTO','PACTOS','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'COMPRA_TERMINO','TERMINO','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'COMPRA_FORWARD','FORWARDS','','-','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'VENTA_PACTO','PACTOS','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'VENTA_TERMINO','TERMINO','','+','ACT','N' FROM DUMMY UNION ALL
SELECT 100000,'VENTA_FORWARD','FORWARDS','','+','ACT','N' FROM DUMMY;
GO


-- CGO
---- TRASPASOS ------------------------------------------------------------------------------------
INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('INGTRS', 'CUS','INGRESO POR TRASPASO DE TITULOS', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('RETTRS', 'CUS','RETIRO POR TRASPASO DE TITULOS', 'VIG');

---- TRANSFERENCIAS ------------------------------------------------------------------------------------

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('INGTRN', 'CUS','INGRESO POR TRANSFERENCIAS DE BOVEDAS', 'VIG');

INSERT INTO  "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto", "IdModulo", "Nombre", "IdEstado")
VALUES ('RETTRN', 'CUS','RETIRO POR TRANSFERENCIAS DE BOVEDAS', 'VIG');

-- CGO

--CUSTODIO
INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ( "IdConcepto","IdModulo","Nombre","IdEstado" )  
VALUES('MCTCUP','POR','MODIFICACION CUENTA CUSTODIO PORTAFOLIO','VIG');
insert into "BSCL.Plataforma.BD::AUT.TipoEvento" 
values('MCTCUP',1,'POR','AUT_MCTCUP','Cambio informacion cuenta custodio portafolio',1,'S','' ,'' ,0,0,'VIG','');
INSERT INTO "BSCL.Plataforma.BD::PLA.RequiereAutorizacion"
VALUES('MCTCUP', 'S')



INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
("IdBolsaTipoInstrumento", "IdTipoInstrumento", "IdBolsa", "IdEstado", "ExigeGarantia", "IdContraparteGarantia", "IdBoveda")
VALUES(50, 'FUF', 'BCS', 'ACT', 'N', 561309, '');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
("IdBolsaTipoInstrumento", "IdTipoInstrumento", "IdBolsa", "IdEstado", "ExigeGarantia", "IdContraparteGarantia", "IdBoveda")
VALUES(46, 'FDO', 'BCS', 'ACT', 'N', 561309, NULL);
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
("IdBolsaTipoInstrumento", "IdTipoInstrumento", "IdBolsa", "IdEstado", "ExigeGarantia", "IdContraparteGarantia", "IdBoveda")
VALUES(48, 'FBC', 'BCS', 'ACT', 'N', 561309, '');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
("IdBolsaTipoInstrumento", "IdTipoInstrumento", "IdBolsa", "IdEstado", "ExigeGarantia", "IdContraparteGarantia", "IdBoveda")
VALUES(47, 'FIP', 'BCS', 'ACT', 'N', 561309, '');
INSERT INTO "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"
("IdBolsaTipoInstrumento", "IdTipoInstrumento", "IdBolsa", "IdEstado", "ExigeGarantia", "IdContraparteGarantia", "IdBoveda")
VALUES(49, 'FIC', 'BCS', 'ACT', 'N', 561309, '');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
