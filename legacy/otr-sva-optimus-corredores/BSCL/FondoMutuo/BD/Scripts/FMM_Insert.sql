SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
DELETE FROM "BSCL.FondoMutuo.BD::FMM.TipoFondo";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Concepto";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.Parametro";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo";
/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoFondo" ("IdTipoFondo", "NombreCorto", "Descripcion") VALUES (1,'Deuda CP <= 90 días'			,'De Inversión en Instrumentos de Deuda de Corto Plazo con Duración menor o igual a 90 días');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoFondo" ("IdTipoFondo", "NombreCorto", "Descripcion") VALUES (2,'Deuda CP <= 365 días'			,'De Inversión en Instrumentos de Deuda de Corto Plazo con Duración menor o igual a 365 días');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoFondo" ("IdTipoFondo", "NombreCorto", "Descripcion") VALUES (3,'Deuda Mediano y Largo > 365'	,'De Inversión en Instrumentos de Deuda de Mediano y Largo Plazo');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoFondo" ("IdTipoFondo", "NombreCorto", "Descripcion") VALUES (4,'Mixto','Mixto');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoFondo" ("IdTipoFondo", "NombreCorto", "Descripcion") VALUES (5,'De Capitalizacion','De Inversión en Instrumentos de Capitalización');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoFondo" ("IdTipoFondo", "NombreCorto", "Descripcion") VALUES (6,'De Libre Inversión','De Libre Inversión');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoFondo" ("IdTipoFondo", "NombreCorto", "Descripcion") VALUES (7,'Estructurado','Estructurado');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoFondo" ("IdTipoFondo", "NombreCorto", "Descripcion") VALUES (8,'Para Calificados','Dirigido a Inversionistas Calificados');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(1, 'ACT', 'CONCEPTO');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(2, 'INA', 'CONCEPTO');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(3, 'ING', 'SOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(4, 'ANU', 'SOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(5, 'RZD', 'SOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(6, 'ACP', 'SOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(7, 'CRT', 'SOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(8, 'PDA', 'SOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(9,  'ACT', 'SERIEDEFONDO');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(10, 'INA', 'SERIEDEFONDO');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(11,  'ACT','FAMILIAFONDO');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(12, 'INA', 'FAMILIAFONDO');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(13, 'ACT', 'COMISIONFONDO');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(14, 'INA', 'COMISIONFONDO');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(15, 'APD', 'CERTIFICACIONFMM');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(16, 'RZD', 'CERTIFICACIONFMM');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(17, 'ING', 'LIQUIDACIONFMM');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(18, 'CRT', 'LIQUIDACIONFMM');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(19, 'ADV', 'CERTIFICACIONFMM');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(20, 'PRO', 'CARGASOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(21, 'ERR', 'CARGASOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(22, 'PEN', 'PROCESOCARGASOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(23, 'PRO', 'PROCESOCARGASOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(24, 'ERR', 'PROCESOCARGASOLICITUDAPORTERESCATE');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(25, 'PEN', 'INTERFAZAGF');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(26, 'PRO', 'INTERFAZAGF');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" ("IdEstadoFondoMutuo", "IdEstado", "NombreEntidad") VALUES(27, 'ERR', 'INTERFAZAGF');

INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('APONORM'   , 'APORTE NORMAL','APO','APORFM','S','S','S', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('APOREINV'  , 'APORTE POR REINVERSIÓN','APO','APORFM','S','S','S', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('APOTRANSF' , 'APORTE POR TRANSFERENCIA','APO','APORFM','N','N','N', 'INA');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('APOTRASP'  , 'APORTE POR TRASPASO','APO','APORFM','N','N','N', 'INA');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('APODIV'    , 'APORTE POR DIVIDENDO','APO','APORFM','S','N','S', 'INA');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('APOCANJE'  , 'APORTE POR CANJE','APO','APORFM','N','N','N', 'INA');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('APOOVER'   , 'APORTE OVERNIGHT','APO','APORFM','N','S','S', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('APOFUSION' , 'APORTE POR FUSIÓN','APO','APORFM','N','N','N', 'INA');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RESCAM'    , 'CAMBIO FONDO','RES','RESCFM','N','S','N', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RESNORM'   , 'RESCATE NORMAL','RES','RESCFM','S','S','S', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RESREINV'  , 'RESCATE POR REINVERSIÓN','RES','RESCFM','S','S','S', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RESTRANSF' , 'RESCATE POR TRANSFERENCIA','RES','RESCFM','N','S','N', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RESTRASP'  , 'RESCATE POR TRASPASO','RES','RESCFM','N','S','N', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RESCANJE'  , 'RESCATE POR CANJE','RES','RESCFM','N','N','N', 'INA');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RESFUSION' , 'RESCATE POR FUSIÓN','RES','RESCFM','N','N','N', 'INA');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RESCOM'    , 'RESCATE POR COMISION','RES','RESCFM','S','N','S', 'INA');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('INGTIT'    , 'INGRESO TITULO','APO','APORFM','S','S','N', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RETTIT'    , 'RETIRO TITULO','RES','RESCFM','S','S','N', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('INGGTA'    , 'INGRESO GARANTIA','APO','APORFM','S','S','N', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('RETGTA'    , 'RETIRO GARANTIA','RES','RESCFM','S','S','N', 'ACT');
INSERT INTO "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo",  "Descripcion", "IndicadorAporteRescate", "IdTipoOperacion","TieneCarga","TieneIngresoManual","TieneCertificacion", "IdEstado") VALUES ('APORESPRO' , 'APORTE CON RESCATE PROGRAMADO','APO','APORFM','S','S','S', 'ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (1,1,'REGIMEN','APV', 'AHORRO PROV. VOLUNTARIO','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (2,1,'REGIMEN','Art107', 'ARTICULO 107 (ex 18Ter)','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (3,1,'TIPORESCATE','M', 'MONTO','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (4,1,'TIPORESCATE','C', 'NUMERO CUOTAS','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (5,1,'TIPORESCATE','T', 'TOTAL','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (6,1,'REGIMEN','Art108', 'Articulo 108','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (7,1,'TIPOOPERACION','APO', 'APORTE FONDO MUTUO','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (8,1,'TIPOOPERACION','RES', 'RESCATE FONDO MUTUO','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (9,1,'PROPOSITO','APV', 'Ahorro Previsional Voluntario','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (10,1,'PROPOSITO','57BIS', 'Beneficios 57 Bis','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (11,1,'PROPOSITO','CVO', 'Cotizacion Voluntaria','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (12,1,'PROPOSITO','DCO', 'Deposito Convenido','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (13,1,'PROPOSITO','54BIS', 'Beneficios 54 Bis','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (14,1,'PROPOSITO','Art108', 'Beneficios Artculo 108','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (15,1,'TIPOREGISTRO','AR', 'AR','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (16,1,'TIPOREGISTRO','PA', 'PA','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (17,1,'TIPOREGISTRO','TR', 'TR','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (18,1,'TIPOREGISTRO','BD', 'BD','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (19,1,'TIPOREGISTRO','CO', 'CO','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (20,1,'TIPOREGISTRO','RC', 'RC','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (21,1,'PROPOSITO','Art107', 'Beneficios Artculo 107','ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Concepto" ("IdConcepto","IdEmpresa","GrupoConcepto","CodigoConcepto","NombreConcepto","Estado") VALUES (22,1,'TIPORESCATE','P','PORCENTAJE','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo"("IdArchivoFondoMutuo","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado") VALUES('ARCHLIQUIDACION-PDF','2016','ArchLiq','V1','ArchLiq','Archivo de Liquidacion de FFMM','ArchLiquidacion','57C8A78D7A150F1CE10000007F000002','pdf',3052,1,'ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo"("IdArchivoFondoMutuo","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado") VALUES('COMP-OVERNIGHT-PDF','2016','CompOver','V1','CompOver','Comprobante de Overnight de FFMM','CompResc','57E1F0F38A820F76E10000007F000002','pdf',3052,1,'ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo"("IdArchivoFondoMutuo","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado") VALUES('COMP-RESCATES-PDF','2016','CompResc','V1','CompResc','Comprobante de Rescates de FFMM','CompResc','57E1F4B98A820F76E10000007F000002','pdf',3052,1,'ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo"("IdArchivoFondoMutuo","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado") VALUES('COMP-APORTES-PDF','2016','CompApor','V1','CompApor','Comprobante de Aportes de FFMM','CompApor','57E1E77D8A820F76E10000007F000002','pdf',3052,1,'ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo"("IdArchivoFondoMutuo","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado") VALUES('COMP-REINVERSION-PDF','2016','CompRein','V1','CompRein','Comprobante de Reinversiones de FFMM','CompRein','57E1F2CB8A820F76E10000007F000002','pdf',3052,1,'ACT');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo"("IdArchivoFondoMutuo","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado") VALUES('INFORMELIQUI-PDF','2016','InfoLiq','V1','InfoLiq','Informe de Liquidacion de FFMM','InfoLiq','581166C41D9A0F92E10000007F000002','pdf',3114,1,'ACT');

insert into BSCL."BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Nombre","IdEstado")
values ('ABPEFM','PAGO AL EMISOR','VIG');

insert into BSCL."BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Nombre","IdEstado")
values ('CAAEFM','APORTE AL EMISOR','VIG');

insert into BSCL."BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Nombre","IdEstado")
values ('CAPEFM','PAGO DEL EMISOR','VIG');

insert into BSCL."BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Nombre","IdEstado")
values ('ABREFM','RESCATE DEL EMISOR','VIG');

insert into BSCL."BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" 
("IdConceptoCuentaInversion","IdConcepto","TipoConcepto","DesplegarEnCombobox","NombreEntidad","Origen")
values (75,'ABPEFM','AB','S','CTA.CARGOABONOMOVIMIENTO','FFM');

insert into BSCL."BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" 
("IdConceptoCuentaInversion","IdConcepto","TipoConcepto","DesplegarEnCombobox","NombreEntidad","Origen")
values (76,'CAAEFM','CA','S','CTA.CARGOABONOMOVIMIENTO','FFM');

insert into BSCL."BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" 
("IdConceptoCuentaInversion","IdConcepto","TipoConcepto","DesplegarEnCombobox","NombreEntidad","Origen")
values (77,'CAPEFM','CA','S','CTA.CARGOABONOMOVIMIENTO','FFM');

insert into BSCL."BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" 
("IdConceptoCuentaInversion","IdConcepto","TipoConcepto","DesplegarEnCombobox","NombreEntidad","Origen")
values (78,'ABREFM','AB','S','CTA.CARGOABONOMOVIMIENTO','FFM');

insert into BSCL."BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
values (125,'CAAEFM','TES.IngresoEgreso','N','FFM','EG');

insert into BSCL."BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
("IdConceptoTesoreria","IdConcepto","NombreEntidad","MultipleConcepto","Origen","Tipo")
values (126,'ABREFM','TES.IngresoEgreso','N','FFM','IN');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") VALUES(1,1,'LiquidacionCuentaInversionCliente','N','NO','VIG');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,2,'TipoComprobanteFacturaNoApv','FEE','FEE','VIG');
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,3,'TipoComprobanteFacturaApv','FAE','FAE','VIG');

insert into BSCL."BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Nombre","IdEstado")
values ('COAFFM','COBRANZA DE COMISIONES ADMINISTRACIÓN DE FONDOS MUTUOS','VIG');

insert into BSCL."BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Nombre","IdEstado")
values ('COCAPV','COBRANZA DE COMISIONES ADMINISTRACIÓN DE APV','VIG');

insert into BSCL."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
("IdConceptoFacturacion","IdConcepto","IdTipoOperacion","Origen","IdEstado")
values (44,'COAFFM','RESCFM','FFM','VIG');

insert into BSCL."BSCL.Facturacion.BD::FAC.ConceptoFacturacion" 
("IdConceptoFacturacion","IdConcepto","IdTipoOperacion","Origen","IdEstado")
values (45,'COCAPV','RESCFM','FFM','VIG');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,4,'TARGET_URL_CARGA_ONLINE','jdbc:jtds:sqlserver://161.238.90.24:1433/PCBIMTRUST','','VIG');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,5,'TARGET_PROP_CARGA_ONLINE',';','','VIG');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,6,'TARGET_DRIVER_CARGA_ONLINE','net.sourceforge.jtds.jdbc.Driver','','VIG');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,7,'TARGET_QVAL_CARGA_ONLINE','select 1','','VIG');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,8,'TARGET_USER_CARGA_ONLINE','consorcio','','VIG');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,9,'TARGET_PASS_CARGA_ONLINE','consorcio','','VIG');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,10,'TARGET_QUERY_CARGA_ONLINE','select top 10 * from plconfigsistema','','VIG');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"("IdMoneda","MontoMinimo","IdSerieFondo","IdEmpresa")
VALUES('CLP',0,'',1);

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"("IdMoneda","MontoMinimo","IdSerieFondo","IdEmpresa")
VALUES('USD',0,'',1);

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"("IdMoneda","MontoMinimo","IdSerieFondo","IdEmpresa")
VALUES('EUR',0,'',1);

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,11,'UsaDolarObservado','S','S','VIG');

---en sura estan encendidos
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,12,'APORTE_DIFERIDO_GENERA_SPOT','N','','VIG');GO
INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado") 
VALUES(1,13,'RESCATE_DIFERIDO_GENERA_SPOT','N','','VIG');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.Parametro"("IdEmpresa","IdParametro","NombreTipoParametro","ValorParametro","NombreParametro","IdEstado","Uso","Descripcion") 
VALUES(1,17,'MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE','CALENDARIO','','VIG','Calculo fecha solicitud de aporte y rescate','Obtiene la fecha de digitación, curse, valor cuota, certificación y liquidacion según la modalidad establecida. Si queda (en blanco) ocupará la modalidad standar de unitario o batch.');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado"	,"PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('APV-A','APV - Regimen A','APV','PROPOSITOAPV','N','S','S','S','APV-A','N','N','N','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('APV-B','APV - Regimen B','APV','PROPOSITOAPV','N','S','S','S','APV-B','N','N','N','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('APVC','APVC - Ahorro Previsional Voluntario Colectivo','APV','PROPOSITOAPV','N','S','S','S','APVC','N','N','N','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('CVO','CVO - Cotizacion Voluntaria','APV','PROPOSITOAPV','N','S','S','S','CVO','N','N','N','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('DCO','DCO - Deposito Convenido','APV','PROPOSITOAPV','N','S','S','N','DCO','N','N','N','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('ART108','Articulo 108 Regimen General','ART108','REGIMENGENERAL','N','S','S','S','ART108','N','S','N','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('ART107','Beneficios Articulo 107 (Ex18Ter)','ART107','FONDOACCIONARIO','N','S','S','S','ART107','N','N','N','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('57BIS','Beneficios 57 Bis','57BIS','57BIS','N','S','S','S','57BIS','N','N','N','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('54BIS','Beneficios 54 Bis','57BIS','57BIS','N','S','S','S','54BIS','N','N','N','ACT');

INSERT INTO "BSCL"."BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"
("IdRegimenFondoMutuo","Descripcion","GrupoRegimen","PropositoSerieRelacionado","PermiteOtrosGrupos","ValidaPortafolioCliente","ValidaPortafolioAGF","PermiteRescate","TipoCarteraNormativa","PermiteCambioGrupo","RescateConMandato","AceptaMandatoEnReinversion","IdEstado")
VALUES('NINGUNO','Sin Beneficio Tributario','TRIBUTARIOS','','N','N','N','S','ART108','N','N','N','ACT');

INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('PROPOSITOAPV','APV','ACT');

INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('57BIS','57BIS','ACT');

INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('REGIMENGENERAL','REGIMEN GENERAL','ACT');

INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")
VALUES ('FONDOACCIONARIO','FONDO ACCIONARIO','ACT');


---------------------------------FLUJO CAJA--------------------------------------

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(3, 'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','saldo de la cta de inv', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(3, 'APORTES_EN_TRANSITO','FONDOS_MUTUOS','Aportes ingresados', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(3, 'APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA','FONDOS_MUTUOS','Aportes ingresados con Instrucción Liquidada', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(3, 'RESCATES_EN_TRANSITO','FONDOS_MUTUOS','Rescates negociados (*a fecha pago T1)', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'ORDENES_EN_TRANSITO_COMPRAS','ORDEN','Ordenes de compra', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'ASIGNACIONES_COMPRAS','ASIGNACION','Ordenes de compras asignadas', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'ASIGNACIONES_VENTAS','ASIGNACION','Ordenes de venta asignadas', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'EGRESOS_TESORERIA','TESORERIA','Egresos ingresados', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'INGRESOS_TESORERIA','TESORERIA','Ingresos ingresados', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (3, 'DIVIDENDOS_EN_DINERO','EVENTO_CAPITAL','Dividendo en dinero', '+','ACT');


INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(4, 'SALDO_CUENTA_INVERSION','CUENTA_INVERSION','saldo de la cta de inv', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES(4, 'RESCATES_EN_TRANSITO','FONDOS_MUTUOS','Rescates negociados (*a fecha pago T1)', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'ORDENES_EN_TRANSITO_COMPRAS','ORDEN','Ordenes de compra', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'ASIGNACIONES_COMPRAS','ASIGNACION','Ordenes de compras asignadas', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'ASIGNACIONES_VENTAS','ASIGNACION','Ordenes de venta asignadas', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'EGRESOS_TESORERIA','TESORERIA','Egresos ingresados', '-','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'INGRESOS_TESORERIA','TESORERIA','Ingresos ingresados', '+','ACT');

INSERT INTO "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
("IdUsoFlujoCaja","ItemCalculo","ItemOrigen","Descripcion","FuncionOperador","IdEstado")
VALUES (4, 'DIVIDENDOS_EN_DINERO','EVENTO_CAPITAL','Dividendo en dinero', '+','ACT');

----------------------------- FIN FLUJO CAJA ----------------------------------------------------------------------

---------------------------- INICIO TIPO OPERACION ANULACION -----------------------------------------------------

INSERT INTO BSCL."BSCL.Plataforma.BD::INS.TipoOperacion"
("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion",
"AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion","EsMovimientoComprobante")
VALUES ('ANUAPO','ANUAPORFM','ANULACION APORTE FONDO MUTUO','APORTE','INFM','N','N','N','N','N','APORTE','','RESCFM','P','N','N','N','N');

INSERT INTO BSCL."BSCL.Plataforma.BD::INS.TipoOperacion"
("IdTipoOperacion","Codigo","Nombre","Accion","IdTipoProducto","Comprobante","EsOrden","EsSimultanea","EsVentaCorta","EsTransaccion",
"AccionOperacion","AccionCustodia","IdTipoOperacionRelacionada","RecibePagaPortafolio","EsAnticipo","EsVencimiento","EsAnulacion","EsMovimientoComprobante")
VALUES ('ANURES','ANURESCFM','ANULACION RESCATE FONDO MUTUO','RESCATE','INFM','N','N','N','N','N','RESCATE','','APORFM','R','N','N','N','N');

--------------------------- FIN TIPO OPERACION ANULACION ---------------------------------------------------------

/*RESCATE FFMM CUSTODIA*/
INSERT INTO BSCL."BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento"
("IdTipoOperacionTipoInstrumento","IdTipoOperacion","IdTipoInstrumento","IdEstado","IdInstrumento","Nemotecnico")
VALUES(348,'RESCFM','FFM','ACT',0,'');

/*PERFIL*/
INSERT INTO BSCL."BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo"
("IdCategoriaPerfilFondoMutuo","IdCategoriaInversionista","NombreCategoriaPerfilFondoMutuo","IdEmpresa","Jerarquia")
VALUES('AGRESIVO','AGRESIVO','AGRESIVO',1,2);

INSERT INTO BSCL."BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo"
("IdCategoriaPerfilFondoMutuo","IdCategoriaInversionista","NombreCategoriaPerfilFondoMutuo","IdEmpresa","Jerarquia")
VALUES('BALANCEADO','BALANCEADO','BALANCEADO',1,3);

INSERT INTO BSCL."BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo"
("IdCategoriaPerfilFondoMutuo","IdCategoriaInversionista","NombreCategoriaPerfilFondoMutuo","IdEmpresa","Jerarquia")
VALUES('MODERADO','MODERADO','MODERADO',1,4);

INSERT INTO BSCL."BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo"
("IdCategoriaPerfilFondoMutuo","IdCategoriaInversionista","NombreCategoriaPerfilFondoMutuo","IdEmpresa","Jerarquia")
VALUES('CONSERVADOR','CONSERVADOR','CONSERVADOR',1,5);

INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (1,'PERFIL_COMERCIAL','0',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (2,'PERFIL_BACKOFFICE','0',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (3 ,'HORA_CIERRE_APORTE_COMERCIAL' ,'00:00:00' ,1 );
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (4,'HORA_CIERRE_RESCATE_COMERCIAL','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (5,'HORA_CIERRE_REINVERSION_COMERCIAL','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (6,'HORA_CIERRE_LIQUIDEZ_COMERCIAL','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (7,'HORA_CIERRE_APORTE_BACKOFFICE','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (8,'HORA_CIERRE_RESCATE_BACKOFFICE','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (9,'HORA_CIERRE_REINVERSION_BACKOFFICE','00:00:00',1);
INSERT INTO "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" ("IdConfiguracionFondoMutuo", "Key", "Value", "IdEmpresa") VALUES (10,'HORA_CIERRE_LIQUIDEZ_BACKOFFICE','00:00:00',1);


delete from bscl."BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo";
GO
insert into bscl."BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"(
    "IdConfiguracionFechasFondoMutuo","IdEmpresa","IdModalidad","IdTipoFecha","TipoSolicitud","EstadoFondo","FondoExtranjero","Descripcion",
    --------------------------------------------------------------------------
    "CantidadDias","EsHabil","IdTipoFechaOrigen","DebeSerHabilFechaOrigen","ValutaCurse","ValutaValorCuota","ValutaCertificacion","ValutaRetencion",
    "CalendarioLocal","CalendarioEmisor","CalendarioFondo","CalendarioMoneda","IdEstado")
SELECT
    ROW_NUMBER() over (ORDER BY "IdModalidad"),1 AS "IdEmpresa","IdModalidad","IdTipoFecha","TipoSolicitud","EstadoFondo","FondoExtranjero","Descripcion",
     --------------------------------------------------------------------------
    "CantidadDias","EsHabil","IdTipoFechaOrigen","DebeSerHabilFechaOrigen",COALESCE("ValutaCurse",'N'),COALESCE("ValutaValorCuota",'N'),
    COALESCE("ValutaCertificacion",'N'),COALESCE("ValutaRetencion",'N'),"CalendarioLocal","CalendarioEmisor","CalendarioFondo","CalendarioMoneda",
    'ACT' AS "IdEstado"
FROM (
    SELECT
        'UNITARIO' AS "IdModalidad",'DIGITA' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de digitación' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'N' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL
    SELECT
        'UNITARIO' AS "IdModalidad",'CURSE' AS "IdTipoFecha",NULL AS "TipoSolicitud",'C' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de curse o aceptación para fondos cerrados' AS "Descripcion",
        --------------------------------------------------------------------------
        1 AS "CantidadDias",'S' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion
    SELECT
        'UNITARIO' AS "IdModalidad",'CURSE' AS "IdTipoFecha",NULL AS "TipoSolicitud",'A' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de curse o aceptación para fondos abiertos' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion
    SELECT
        'UNITARIO' AS "IdModalidad",'VCUOTA' AS "IdTipoFecha",NULL AS "TipoSolicitud",'C' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de certificación para fondos cerrados' AS "Descripcion",
        --------------------------------------------------------------------------
        1 AS "CantidadDias",'S' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",'S' AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--igual a certificacion
    SELECT
        'UNITARIO' AS "IdModalidad",'VCUOTA' AS "IdTipoFecha",NULL AS "TipoSolicitud",'A' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de valor cuota para fondos abiertos' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",'S' AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--igual a certificacion
    SELECT
        'UNITARIO' AS "IdModalidad",'CERTIF' AS "IdTipoFecha",NULL AS "TipoSolicitud",'C' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de valor cuota para fondos cerrados' AS "Descripcion",
        --------------------------------------------------------------------------
        1 AS "CantidadDias",'S' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",'S' AS "ValutaValorCuota",
        'S' AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasCERTIF
    SELECT
        'UNITARIO' AS "IdModalidad",'CERTIF' AS "IdTipoFecha",NULL AS "TipoSolicitud",'A' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de certificación para fondos abiertos' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",'S' AS "ValutaValorCuota",
        'S' AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasCERTIF
    SELECT
        'UNITARIO' AS "IdModalidad",'LIQUID' AS "IdTipoFecha",NULL AS "TipoSolicitud",'C' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de liquidación para fondos cerrados' AS "Descripcion",
        --------------------------------------------------------------------------
        1 AS "CantidadDias",'S' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",'S' AS "ValutaValorCuota",
        'S' AS "ValutaCertificacion",'S' AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasLIQUID
    SELECT
        'UNITARIO' AS "IdModalidad",'LIQUID' AS "IdTipoFecha",NULL AS "TipoSolicitud",'A' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de liquidación para fondos abiertos' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",'S' AS "ValutaValorCuota",
        'S' AS "ValutaCertificacion",'S' AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasLIQUID
    -----------------------------------------------------------------------------------------------------------------------------------------------------
    SELECT
        'BATCH' AS "IdModalidad",'DIGITA' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de digitación' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'N' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL
    SELECT
        'BATCH' AS "IdModalidad",'CURSE' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de curse o aceptación para fondos abiertos y cerrados' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'DIGITA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion
    SELECT
        'BATCH' AS "IdModalidad",'VCUOTA' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de valor cuota para fondos abiertos y cerrados' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'DIGITA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion
    SELECT
        'BATCH' AS "IdModalidad",'CERTIF' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de certificación' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'DIGITA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasCERTIF
    SELECT
        'BATCH' AS "IdModalidad",'LIQUID' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de liquidación' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'DIGITA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",'S' AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasLIQUID
    -----------------------------------------------------------------------------------------------------------------------------------------------------
    SELECT
        'CALENDARIO' AS "IdModalidad",'DIGITA' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de digitación' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'N' AS "EsHabil",NULL AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL
    SELECT
        'CALENDARIO' AS "IdModalidad",'CURSE' AS "IdTipoFecha",NULL AS "TipoSolicitud",'C' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de curse para fondos cerrados' AS "Descripcion",
        --------------------------------------------------------------------------
        1 AS "CantidadDias",'S' AS "EsHabil",'DIGITA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion
    SELECT
        'CALENDARIO' AS "IdModalidad",'CURSE' AS "IdTipoFecha",NULL AS "TipoSolicitud",'A' AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de curse para fondos abiertos' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'DIGITA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",'S' AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion
    SELECT
        'CALENDARIO' AS "IdModalidad",'VCUOTA' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",'N' AS "FondoExtranjero",
        'Fecha de valor cuota' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'CURSE' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",'S' AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion
    /*SELECT
        'CALENDARIO' AS "IdModalidad",'VCUOTA' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",'S' AS "FondoExtranjero",
        'Fecha de valor cuota' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'N' AS "EsHabil",'CURSE' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",'S' AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'S' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion
    SELECT
        'CALENDARIO' AS "IdModalidad",'VCUOTA' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",'S' AS "FondoExtranjero",
        'Fecha de valor cuota' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'VCUOTA' AS "IdTipoFechaOrigen",'S' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'N' AS "CalendarioLocal",'S' AS "CalendarioEmisor",'S' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion*/
    SELECT
        'CALENDARIO' AS "IdModalidad",'VCUOTA' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",'S' AS "FondoExtranjero",
        'Fecha de valor cuota' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'CURSE' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",'S' AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'N' AS "CalendarioLocal",'S' AS "CalendarioEmisor",'S' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL--NumeroDiasAceptacion
    /*SELECT
        'CALENDARIO' AS "IdModalidad",'CERTIF' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",'N' AS "FondoExtranjero",
        'Fecha de certificación para aportes' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'VCUOTA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        'S' AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL
    SELECT
        'CALENDARIO' AS "IdModalidad",'CERTIF' AS "IdTipoFecha",'APO' AS "TipoSolicitud",NULL AS "EstadoFondo",'S' AS "FondoExtranjero",
        'Fecha de certificación para aportes' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'VCUOTA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        'S' AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL
    SELECT
        'CALENDARIO' AS "IdModalidad",'CERTIF' AS "IdTipoFecha",'RES' AS "TipoSolicitud",NULL AS "EstadoFondo",'S' AS "FondoExtranjero",
        'Fecha de certificación para rescates' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'VCUOTA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        'S' AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL*/
    SELECT
        'CALENDARIO' AS "IdModalidad",'CERTIF' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",NULL AS "FondoExtranjero",
        'Fecha de certificación' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'VCUOTA' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        'S' AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL
    SELECT
        'CALENDARIO' AS "IdModalidad",'LIQUID' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",'N' AS "FondoExtranjero",
        'Fecha de liquidación' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'CERTIF' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",'S' AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL --NO EXISTE NumeroDiasRetencion PARA APORTES
    SELECT
        'CALENDARIO' AS "IdModalidad",'LIQUID' AS "IdTipoFecha",'APO' AS "TipoSolicitud",NULL AS "EstadoFondo",'S' AS "FondoExtranjero",
        'Fecha de liquidación para aportes' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'CERTIF' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",'S' AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY UNION ALL --NO EXISTE NumeroDiasRetencion PARA APORTES
    SELECT
        'CALENDARIO' AS "IdModalidad",'LIQUID' AS "IdTipoFecha",'RES' AS "TipoSolicitud",NULL AS "EstadoFondo",'S' AS "FondoExtranjero",
        'Fecha de liquidación para rescates' AS "Descripcion",
        --------------------------------------------------------------------------
        --0 AS "CantidadDias",'N' AS "EsHabil",'CERTIF' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        0 AS "CantidadDias",'S' AS "EsHabil",'CERTIF' AS "IdTipoFechaOrigen",'N' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",'S' AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'S' AS "CalendarioMoneda"
    FROM DUMMY/* UNION ALL
    SELECT
        'CALENDARIO' AS "IdModalidad",'LIQUID' AS "IdTipoFecha",NULL AS "TipoSolicitud",NULL AS "EstadoFondo",'S' AS "FondoExtranjero",
        'Fecha de liquidación para rescates' AS "Descripcion",
        --------------------------------------------------------------------------
        0 AS "CantidadDias",'S' AS "EsHabil",'LIQUID' AS "IdTipoFechaOrigen",'S' AS "DebeSerHabilFechaOrigen",NULL AS "ValutaCurse",NULL AS "ValutaValorCuota",
        NULL AS "ValutaCertificacion",NULL AS "ValutaRetencion",'S' AS "CalendarioLocal",'N' AS "CalendarioEmisor",'N' AS "CalendarioFondo",'N' AS "CalendarioMoneda"
    FROM DUMMY*/
);

select * from bscl."BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo" WHERE "IdModalidad" = 'CALENDARIO';

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
COMMIT;
