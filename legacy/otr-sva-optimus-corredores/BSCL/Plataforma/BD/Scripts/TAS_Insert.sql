SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table                                                 */
/*==============================================================*/ 
DELETE FROM "BSCL.Plataforma.BD::TAS.Fuente";
DELETE FROM "BSCL.Plataforma.BD::TAS.ConfiguracionTasas";
DELETE FROM "BSCL.Plataforma.BD::TAS.TipoTasa";
DELETE FROM "BSCL.Plataforma.BD::TAS.EstadoTasa";
DELETE FROM "BSCL.Plataforma.BD::TAS.TipoCriterioTasa";
DELETE FROM "BSCL.Plataforma.BD::TAS.Uso";


/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::TAS.Fuente                 */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (1,'RISK','TASASMERCADO',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (2,'BBVA','ASSET CONTROL',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (3,'STIA','SCOTIA',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (4,'CERO','Curva Ceros',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (5,'BCU','Curva BCU',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (6,'PRC','Curva PRC',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (7,'BR','Curva BR',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (8,'BCP','Curva BCP',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (9,'DPD','Curva DPD',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (10,'PDBC','Curva PDBC',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (11,'DPF1','Curva DPF1',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (12,'DPF2','Curva DPF2',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (13,'DPR','Curva DPR',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (14,'SUF','Curva Swap UF',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (15,'SCLP','Curva Swap CLP',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (16,'SLIB','Curva Swap Libor',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (17,'FUF','Curva ML_FINANCE UF',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (18,'SCUF','Curva SC UF USD',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (19,'FCLP','Curva ML_FINANCE CLP',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (20,'FUSD','Curva ML_FINANCE USD',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (21,'FGBP','Curva ML_FINANCE GBP',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (22,'SJPY','Curva Swap JPY',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (23,'SEUR','Curva Swap EUR',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (24,'FJPY','Curva ML_FINANCE JPY',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (25,'SUSD','Curva Swap USD',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (26,'LHCO','Curva LH Corp',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (27,'SPT','Curva Spot',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (28,'PFWD','Puntos ML_FINANCE',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (29,'SUP','SUP',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (30,'FEUR','Curva ML_FINANCE EUR',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (31,'MLFI','ML_FINANCE',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (32,'CORF','Comercial RF',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (33,'COIF','Comercial IF',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (34,'CCTP','Control Precio Pacto',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (35,'CCTI','Control Tasa Instrumentos',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (36,'SAFP','Super de AFP',1,'ACT','MANTENEDOR');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (37,'TIPOINSTRUMENTO','Tipo Instrumento',1,'ACT','CRITERIO');
INSERT INTO "BSCL.Plataforma.BD::TAS.Fuente" ("IdFuente","Codigo","Descripcion","IdEmpresa","IdEstado","NombreEntidad")
VALUES (38,'INSTRUMENTO','Instrumento',1,'ACT','CRITERIO');


/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::BAT.TipoPrograma           */
/*==============================================================*/

INSERT INTO BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo")
VALUES('GENERAR_ARCHIVO_TASAS','Generar Archivo Tasas',1,'Plataforma','cl.bcs.BcsGenerarArchivoTasas.GenerarArchivoTasas','JAVA','G');


/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::TAS.ConfiguracionTasas     */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" ("IdConfiguracionTasas","IdEmpresa","NombreEntidad","Valor","Descripcion")
VALUES(1,1,'RUTAARCHIVOTASAS','tas/','Ruta archivo de modulo TASAS. Se concatena con RutaBaseArchivos de configuracion PLA');
INSERT INTO "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" ("IdConfiguracionTasas","IdEmpresa","NombreEntidad","Valor","Descripcion")
VALUES(2,1,'SERVICIOCONSULTARTASAS','BSCL.Plataforma.BD.Servicios::TAS.ConsultarTasasArchivo','Servicio para Consultar Tasas que conforman la data del archivo');
INSERT INTO "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" ("IdConfiguracionTasas","IdEmpresa","NombreEntidad","Valor","Descripcion")
VALUES(3,1,'TIPOCARTERATASAS','PROPIA','CARTERA PROPIA');
INSERT INTO "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" ("IdConfiguracionTasas","IdEmpresa","NombreEntidad","Valor","Descripcion")
VALUES(4,1,'TIPOCARTERATASAS','TERCEROS','CUENTA TERCEROS');
INSERT INTO "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" ("IdConfiguracionTasas","IdEmpresa","NombreEntidad","Valor","Descripcion")
VALUES(5,1,'TIPOREGISTROPRECIOTASA','PRECIO','PRECIO');
INSERT INTO "BSCL.Plataforma.BD::TAS.ConfiguracionTasas" ("IdConfiguracionTasas","IdEmpresa","NombreEntidad","Valor","Descripcion")
VALUES(6,1,'TIPOREGISTROPRECIOTASA','TASA','TASA');


/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::TAS.TipoTasa				*/
/*==============================================================*/
--VALORES : CURVA, PRECIO, PUNTO y TASA
INSERT INTO "BSCL.Plataforma.BD::TAS.TipoTasa" ("IdTipoTasa","Codigo","Descripcion","IdEmpresa","IdEstado")
VALUES ('CURVA','CURVA','CURVA',1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::TAS.TipoTasa" ("IdTipoTasa","Codigo","Descripcion","IdEmpresa","IdEstado")
VALUES ('PRECIO','PRECIO','PRECIO',1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::TAS.TipoTasa" ("IdTipoTasa","Codigo","Descripcion","IdEmpresa","IdEstado")
VALUES ('PUNTO','PUNTO','PUNTO',1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::TAS.TipoTasa" ("IdTipoTasa","Codigo","Descripcion","IdEmpresa","IdEstado")
VALUES ('TASA','TASA','TASA',1,'ACT');


/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::TAS.EstadoTasa				*/
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(1,'CRA','MANTENEDORTASAS','CREADO EN ARCHIVO');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(2,'SOL','MANTENEDORTASAS','SOLICITADO');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(3,'CAR','MANTENEDORTASAS','CARGADO');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(4,'OFI','MANTENEDORTASAS','OFICIALIZADO');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(5,'PUB','MANTENEDORTASAS','PUBLICADO');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(6,'CRS','MANTENEDORTASAS','CREADO EN SISTEMA');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(7,'ACT','MANTENEDORCRITERIOS','ACTIVO');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(8,'INA','MANTENEDORCRITERIOS','INACTIVO');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(9,'MOD','MANTENEDORTASAS','MODIFICADO EN SISTEMA');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(10,'ACT','MANTENEDORMARGENESTASAS','ACTIVO');
INSERT INTO "BSCL.Plataforma.BD::TAS.EstadoTasa" ("IdEstadoTasa","IdEstado","NombreEntidad","Descripcion")
VALUES(11,'INA','MANTENEDORMARGENESTASAS','INACTIVO');


/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::TAS.TipoCriterioTasa		*/
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::TAS.TipoCriterioTasa" ("IdTipoCriterioTasa","Codigo","Descripcion","IdEmpresa","IdEstado")
VALUES('COMERCIAL','COMERCIAL','COMERCIAL',1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::TAS.TipoCriterioTasa" ("IdTipoCriterioTasa","Codigo","Descripcion","IdEmpresa","IdEstado")
VALUES('NORMATIVO','NORMATIVO','NORMATIVO',1,'ACT');


/*==============================================================*/
/* Insert Table: BSCL.Plataforma.BD::TAS.Uso					*/
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::TAS.Uso"("IdUso","Codigo","Descripcion","IdEmpresa","IdEstado")
VALUES('CARTERA','CARTERA','CARTERA',1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::TAS.Uso"("IdUso","Codigo","Descripcion","IdEmpresa","IdEstado")
VALUES('GARANTIA','GARANTIA','GARANTIA',1,'ACT');
INSERT INTO "BSCL.Plataforma.BD::TAS.Uso"("IdUso","Codigo","Descripcion","IdEmpresa","IdEstado")
VALUES('SUBYACENTE','SUBYACENTE','SUBYACENTE',1,'ACT');


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
