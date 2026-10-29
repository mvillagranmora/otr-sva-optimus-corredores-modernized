SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM  "BSCL.Plataforma.BD::REG.TipoRegimenConyugal"; 
DELETE FROM  "BSCL.Plataforma.BD::REG.TipoEstadoCivil"; 
DELETE FROM  "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad"; 
DELETE FROM  "BSCL.Plataforma.BD::REG.Region"; 
DELETE FROM  "BSCL.Plataforma.BD::REG.DiaFeriado";
DELETE FROM  "BSCL.Plataforma.BD::REG.TipoDiaFeriado";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO "BSCL.Plataforma.BD::REG.TipoRegimenConyugal" ( "IdTipoRegimenConyugal","Nombre","IdPais" )  
VALUES('SC','Sociedad Conyugal',44);
INSERT INTO "BSCL.Plataforma.BD::REG.TipoRegimenConyugal" ( "IdTipoRegimenConyugal","Nombre","IdPais" )  
VALUES('SB','Separación Total de Bienes',44);
INSERT INTO "BSCL.Plataforma.BD::REG.TipoRegimenConyugal" ( "IdTipoRegimenConyugal","Nombre","IdPais" )  
VALUES('PG','Participación en los Gananciales',44);

INSERT INTO "BSCL.Plataforma.BD::REG.TipoEstadoCivil" ( "IdTipoEstadoCivil","Nombre","IdPais","IdEstado" )  
VALUES('CA','Casado(a)',44,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.TipoEstadoCivil" ( "IdTipoEstadoCivil","Nombre","IdPais","IdEstado" )  
VALUES('SO','Soltero(a)',44,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.TipoEstadoCivil" ( "IdTipoEstadoCivil","Nombre","IdPais","IdEstado" )  
VALUES('VI','Viudo(a)',44,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.TipoEstadoCivil" ( "IdTipoEstadoCivil","Nombre","IdPais","IdEstado" )  
VALUES('DI','Divorciado(a)',44,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.TipoEstadoCivil" ( "IdTipoEstadoCivil","Nombre","IdPais","IdEstado" )  
VALUES('SE','Separado(a)',44,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.TipoEstadoCivil" ( "IdTipoEstadoCivil","Nombre","IdPais","IdEstado" )  
VALUES('CC','CONVIVIENTE CIVIL',44,'ACT');

INSERT INTO "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" ( "IdTipoDocumentoIdentidad","NombreTipoDocumentoIdentidad","NombreAbrTipoDocumentoIdentidad","IdPais","PorDefecto","IdEstado" )  
VALUES('R','Rut','RUT',44,'S','ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" ( "IdTipoDocumentoIdentidad","NombreTipoDocumentoIdentidad","NombreAbrTipoDocumentoIdentidad","IdPais","PorDefecto","IdEstado" )  
VALUES('P','Pasaporte','PSP',44,'N','ACT');


/*
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(13,'Región Metropolitana',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(15,'XV Arica y Parinacota',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(1,'I Tarapacá',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(2,'II Antofagasta',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(3,'III Atacama',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(4,'IV Coquimbo',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(5,'V Valparaíso',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(6,'VI O''Higgins',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(7,'VII Maule',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(8,'VIII Biobío',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(9,'IX La Araucanía',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(14,'XIV Los Ríos',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(10,'X Los Lagos',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(11,'XI Aysén',44);
INSERT INTO "BSCL.Plataforma.BD::REG.Region" ( "IdRegion","NombreRegion","IdPais" )  
VALUES(12,'XII Magallanes y Antártica',44);
*/




INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(1,'Inhabil','0001-01-01',5,'Sábado',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(2,'Inhabil','0001-01-01',6,'Domingo',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(3,'Feriado','2015-01-01',3,'Año Nuevo',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(4,'Feriado','2015-04-03',4,'Viernes Santo',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(5,'Feriado','2015-04-04',5,'Sábado Santo',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(6,'Feriado','2015-05-01',4,'Día Nacional del Trabajo',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(7,'Feriado','2015-05-21',3,'Día de las Glorias Navales',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(8,'Feriado','2015-06-29',0,'San Pedro y San Pablo',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(9,'Feriado','2015-07-16',3,'Día de la Virgen del Carmen',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(10,'Feriado','2015-08-15',5,'Asunción de la Virgen',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(11,'Feriado','2015-09-18',4,'Independencia Nacional',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(12,'Feriado','2015-09-19',5,'Día de las Glorias del Ejército',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(13,'Feriado','2015-10-12',0,'Encuentro de dos mundos',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(14,'Feriado','2015-10-31',5,'Día de las Iglesias Evangélicas y Protestantes',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(15,'Feriado','2015-11-01',6,'Día de Todos los Santos',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(16,'Feriado','2015-12-08',1,'Inmaculada Concepción',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(17,'Feriado','2015-12-25',4,'Navidad',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(18,'Feriado','2015-12-31',3,'Feriado Bancario',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(19,'Feriado','2016-01-01',4,'Año Nuevo',2015,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(20,'Inhabil','0001-01-01',5,'Sábado',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(21,'Inhabil','0001-01-01',6,'Domingo',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(22,'Feriado','2014-01-01',2,'Año Nuevo',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(23,'Feriado','2014-04-18',3,'Viernes Santo',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(24,'Feriado','2014-04-19',5,'Sábado Santo',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(25,'Feriado','2014-05-01',3,'Día Nacional del Trabajo',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(26,'Feriado','2014-05-21',2,'Día de las Glorias Navales',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(27,'Feriado','2014-06-29',6,'San Pedro y San Pablo',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(28,'Feriado','2014-07-16',2,'Día de la Virgen del Carmen',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(29,'Feriado','2014-08-15',4,'Asunción de la Virgen',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(30,'Feriado','2014-09-18',3,'Independencia Nacional',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(31,'Feriado','2014-09-19',4,'Día de las Glorias del Ejército',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(32,'Feriado','2014-10-12',6,'Encuentro de dos mundos',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(33,'Feriado','2014-10-31',4,'Día de las Iglesias Evangélicas y Protestantes',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(34,'Feriado','2014-11-01',5,'Día de Todos los Santos',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(35,'Feriado','2014-12-08',0,'Inmaculada Concepción',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(36,'Feriado','2014-12-25',3,'Navidad',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(37,'Feriado','2014-12-31',2,'Feriado Bancario',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(38,'Feriado','2013-01-01',3,'Año Nuevo',2014,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(39,'Inhabil','0001-01-01',5,'Sábado',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(40,'Inhabil','0001-01-01',6,'Domingo',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(41,'Feriado','2015-01-01',3,'Año Nuevo',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(42,'Feriado','2015-04-03',4,'Viernes Santo',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(43,'Feriado','2015-04-04',5,'Sábado Santo',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(44,'Feriado','2015-05-01',4,'Día Nacional del Trabajo',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(45,'Feriado','2015-05-21',3,'Día de las Glorias Navales',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(46,'Feriado','2015-06-29',0,'San Pedro y San Pablo',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(47,'Feriado','2015-07-16',3,'Día de la Virgen del Carmen',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(48,'Feriado','2015-08-15',5,'Asunción de la Virgen',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(49,'Feriado','2015-09-18',4,'Independencia Nacional',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(50,'Feriado','2015-09-19',5,'Día de las Glorias del Ejército',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(51,'Feriado','2015-10-12',0,'Encuentro de dos mundos',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(52,'Feriado','2015-10-31',5,'Día de las Iglesias Evangélicas y Protestantes',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(53,'Feriado','2015-11-01',6,'Día de Todos los Santos',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(54,'Feriado','2015-12-08',1,'Inmaculada Concepción',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(55,'Feriado','2015-12-25',4,'Navidad',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(56,'Feriado','2015-12-31',3,'Feriado Bancario',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(57,'Feriado','2016-01-01',4,'Año Nuevo',2015,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(58,'Inhabil','0001-01-01',5,'Sábado',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(59,'Inhabil','0001-01-01',6,'Domingo',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(60,'Feriado','2014-01-01',2,'Año Nuevo',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(61,'Feriado','2014-04-18',3,'Viernes Santo',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(62,'Feriado','2014-04-19',5,'Sábado Santo',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(63,'Feriado','2014-05-01',3,'Día Nacional del Trabajo',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(64,'Feriado','2014-05-21',2,'Día de las Glorias Navales',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(65,'Feriado','2014-06-29',6,'San Pedro y San Pablo',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(66,'Feriado','2014-07-16',2,'Día de la Virgen del Carmen',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(67,'Feriado','2014-08-15',4,'Asunción de la Virgen',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(68,'Feriado','2014-09-18',3,'Independencia Nacional',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(69,'Feriado','2014-09-19',4,'Día de las Glorias del Ejército',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(70,'Feriado','2014-10-12',6,'Encuentro de dos mundos',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(71,'Feriado','2014-10-31',4,'Día de las Iglesias Evangélicas y Protestantes',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(72,'Feriado','2014-11-01',5,'Día de Todos los Santos',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(73,'Feriado','2014-12-08',0,'Inmaculada Concepción',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(74,'Feriado','2014-12-25',3,'Navidad',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(75,'Feriado','2014-12-31',2,'Feriado Bancario',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(76,'Feriado','2013-01-01',3,'Año Nuevo',2014,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais")
VALUES(77,'Inhabil','0001-01-01',5,'Sábado',2016,1);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais")
VALUES(78,'Inhabil','0001-01-01',6,'Domingo',2016,1);

INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais")
VALUES(84,'INHABIL','0001-01-01',5,'SABADO',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ("IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais")
VALUES(85,'INHABIL','0001-01-01',6,'DOMINGO',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(86,'FERIADO','2017-01-01',6,'AÑO NUEVO',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(87,'FERIADO','2017-04-14',4,'VIERNES SANTO',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(88,'FERIADO','2017-04-15',5,'SABADO SANTO',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(89,'FERIADO','2017-05-01',0,'DIA NACIONAL DEL TRABAJO',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(90,'FERIADO','2017-05-21',6,'DIA DE LAS GLORIAS NAVALES',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(91,'FERIADO','2017-06-26',0,'SAN PEDRO Y SAN PABLO',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(92,'FERIADO','2017-07-16',6,'DIA DE LA VIRGEN DEL CARMEN',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(93,'FERIADO','2017-08-15',1,'ASUNCION DE LA VIRGEN',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(94,'FERIADO','2017-09-18',0,'INDEPENDENCIA NACIONAL',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(95,'FERIADO','2017-09-19',1,'DIA DE LAS GLORIAS DEL EJERCITO',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(96,'FERIADO','2017-10-09',0,'ENCUENTRO DE DOS MUNDOS',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(97,'FERIADO','2017-10-27',4,'DIA DE LAS IGLESIAS EVANGELICAS Y PROTESTANTES',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(98,'FERIADO','2017-11-01',2,'DIA DE TODOS LOS SANTOS',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(99,'FERIADO','2017-12-08',4,'INMACULADA CONCEPCION',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(100,'FERIADO','2017-12-25',0,'NAVIDAD',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(101,'FERIADO','2017-12-31',6,'FERIADO BANCARIO',2017,44);
INSERT INTO "BSCL.Plataforma.BD::REG.DiaFeriado" ( "IdFeriado","Tipo","Fecha","Dia","DescripcionDia","AgnoCarga","IdPais" )  
VALUES(102,'FERIADO','2018-01-01',0,'AÑO NUEVO',2017,44);



INSERT INTO "BSCL.Plataforma.BD::REG.TipoDiaFeriado" ( "IdTipoDiaFeriado","Nombre","IdPais","IdEstado" )
VALUES('FERIADO','FERIADO',44,'ACT');
INSERT INTO "BSCL.Plataforma.BD::REG.TipoDiaFeriado" ( "IdTipoDiaFeriado","Nombre","IdPais","IdEstado" )
VALUES('INHABIL','INHABIL',44,'ACT');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
