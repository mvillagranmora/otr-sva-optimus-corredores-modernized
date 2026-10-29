SET SCHEMA "BSCL";


/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/ 
DELETE FROM  "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"; 



/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::PLA.Archivo                */
/*==============================================================*/


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo" ("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera") 
values(2,1,'ARCHIVO_CLIENTE','S',';','S');

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Archivo" ("IdArchivo","IdEmpresa","CodigoArchivo","TieneSeparador","SeparadorColumnas","TieneCabecera")
SELECT MAX("IdArchivo") + 1, 1, 'CARGA_MASIVA_TESORERIA', 'S', ';', 'N' FROM bscl."BSCL.Plataforma.BD::PLA.Archivo";



/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::PLA.Formato                */
/*==============================================================*/


insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(9,'TIPODOCUMENTO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(10,'DOCUMENTOIDENTIDAD','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(11,'TIPOPERSONA','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(12,'NOMBRES','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(13,'APELLIDOPATERNO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(14,'APELLIDOMATERNO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(15,'PAISNACIMIENTO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(16,'FECHANACIMIENTO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(17,'EDOCIVIL','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(18,'GENERO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(19,'DIRECCION','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(20,'PAISRESIDENCIA','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(21,'CIUDAD','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(22,'TELEFONO','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(23,'EMAIL','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(24,'CATINV','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(25,'PERCOMER','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(32,'RAZONSOCIAL','string',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(33,'TIPOCLIENTE','STRING',0,0,'S','','');
insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato" values(100,'EJECUTIVO','NUMBER',0,0,'N','','','N',0);



-- Archivo: Carga masiva por recaudación (TESORERIA)
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
SELECT MAX("IdFormato") + 1, 'IDTIPOMOVIMIENTO', 'STRING', 1, 6, 'S', '', '', 'N' ,0 FROM bscl."BSCL.Plataforma.BD::PLA.Formato";

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
SELECT MAX("IdFormato") + 1, 'DOCUMENTOIDENTIDAD', 'STRING', 2, 40, 'S', '', '', 'N', 0 FROM bscl."BSCL.Plataforma.BD::PLA.Formato";

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
SELECT MAX("IdFormato") + 1, 'MONTO', 'NUMBER', 3, 26,'S','','','S',8 FROM bscl."BSCL.Plataforma.BD::PLA.Formato";


/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::PLA.ArchivoFormato         */
/*==============================================================*/

insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(5,2,5);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(6,2,6);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(7,2,7);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(8,2,8);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(9,2,9);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(10,2,10);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(11,2,11);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(12,2,12);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(13,2,13);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(14,2,14);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(15,2,15);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(16,2,16);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(17,2,17);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(18,2,18);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(19,2,19);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(20,2,20);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(21,2,21);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(39,3,33);
insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" values(109,3,100);



-- Archivo: Carga masiva por recaudación (TESORERIA)
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato") SELECT MAX("IdArchivoFormato") + 1, 12, 113 FROM bscl."BSCL.Plataforma.BD::PLA.ArchivoFormato";
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato") SELECT MAX("IdArchivoFormato") + 1, 12, 114 FROM bscl."BSCL.Plataforma.BD::PLA.ArchivoFormato";
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato") SELECT MAX("IdArchivoFormato") + 1, 12, 115 FROM bscl."BSCL.Plataforma.BD::PLA.ArchivoFormato";














-- ====================== Modificar formato de acá hacia bajo ======================================
-- ====================== Modificar formato de acá hacia bajo ======================================
-- ====================== Modificar formato de acá hacia bajo ======================================


-- FIN ARCHIVO_CLIENTE--------------



-------------ARCHIVO_ASIGNACION_MANUAL-------------
--Registro de archivos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera")  
VALUES(4,1,'ARCHIVO_ASIGNACION_MANUAL','S',';','S');

--Formatos de archivo con separacion ;
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(26,'FOLIOTRANSACCION','string',0,0,'S','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(27,'INSTRUMENTO','string',0,0,'S','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(28,'DOCUMENTOIDENTIDAD','string',0,0,'S','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(29,'PORTAFOLIO','number',0,0,'S','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(30,'ORDEN','number',0,0,'S','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Formato" VALUES(31,'CANTIDADASIGNADA','number',0,0,'S','','');

--Relaciones archivo con separacion y sus formatos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(26,4,26);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(27,4,27);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(28,4,28);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(29,4,29);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(30,4,30);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(31,4,31);

-------------FIN  ARCHIVO_ASIGNACION_MANUAL--------------


-------------ARCHIVO_ASIGNACION_POSTRERA-------------
--Registro de archivos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera") 
VALUES(5,1,'ARCHIVO_ASIGNACION_POSTRERA','S',';','S');

--Relaciones archivo con separacion y sus formatos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(32,5,26);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(33,5,27);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(34,5,28);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(35,5,29);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(37,5,31);

-------------FIN  ARCHIVO_ASIGNACION_POSTRERA--------------


-------------ARCHIVO_SORTEO_LETRAS-------------
--Registro de archivos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera") 
VALUES(7,1,'ARCHIVO_SORTEO_LETRAS','S','\t','N');

--Formatos de archivo con separacion ;
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(41,'DEPOSITANTE','NUMBER',1,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(42,'MONEDA','NUMBER',2,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(43,'GLOSAMONEDA','STRING',3,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(44,'EMISORPAGADOR','NUMBER',4,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(45,'EMISOR','NUMBER',5,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(46,'INSTRUMENTO','NUMBER',6,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(47,'GLOSAINSTRUMENTO','STRING',7,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(48,'EVENTO','STRING',8,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(49,'NEMO','STRING',9,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(50,'BLOQUEO','NUMBER',10,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(51,'SORTEADO','NUMBER',11,0,'S','','','S',4);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(52,'APAGAR','NUMBER',12,0,'S','','','S',4);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(53,'APAGARPESOS','NUMBER',13,0,'S','','','N',0);

--Relaciones archivo con separacion y sus formatos
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(47,7,41);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(48,7,42);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(49,7,43);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(50,7,44);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(51,7,45);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(52,7,46);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(53,7,47);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(54,7,48);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(55,7,49);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(56,7,50);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(57,7,51);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(58,7,52);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(59,7,53);

-------------FIN  ARCHIVO_SORTEO_LETRAS-------------



---------------CARGA_MASIVA_DEPOSITOS---------------
--Registro de archivos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera") 
VALUES(9,1,'CARGA_MASIVA_DEPOSITOS','S',';','N');

--Formatos de archivo con separacion ;
SET SCHEMA "BSCL";
--INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(62,'FECHAPROCESO','STRING',1,10,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(63,'IDSECUENCIA','NUMBER',2,8,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(64,'RUTCOMPRADOR','STRING',3,11,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(65,'SECUENCIARUTCOMPRADOR','NUMBER',4,2,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(66,'CUSTODIACOMPRADOR','STRING',5,1,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(67,'RUTVENDEDOR','STRING',6,11,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(68,'SECUENCIARUTVENDEDOR','NUMBER',7,2,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(69,'CUSTODIAVENDEDOR','STRING',8,1,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(70,'EMISOR','STRING',9,10,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(71,'FECHAINICIOOPERACION','STRING',10,10,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(72,'FECHATERMINOPAPEL','STRING',11,10,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(73,'TIPOREAJUSTE','STRING',12,2,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(74,'MONTOFINAL','NUMBER',13,17,'S','','','S',2);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" VALUES(75,'MONTOINICIAL','NUMBER',14,17,'S','','','S',2);

--Relaciones archivo con separacion y sus formatos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(68,9,62);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(69,9,63);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(70,9,64);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(71,9,65);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(72,9,66);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(73,9,67);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(74,9,68);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(75,9,69);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(76,9,70);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(77,9,71);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(78,9,72);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(79,9,73);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(80,9,74);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" VALUES(81,9,75);

-------------FIN  CARGA_MASIVA_DEPOSITOS-------------


