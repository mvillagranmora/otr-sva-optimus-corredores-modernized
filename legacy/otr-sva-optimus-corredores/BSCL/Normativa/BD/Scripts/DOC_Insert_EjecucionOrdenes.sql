SET SCHEMA "BSCL";


/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/


--DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"IN ('CIR1985','OFI253');

DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema" IN ('CIR1985','OFI253');
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento" IN ('CIR1985','OFI253');
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumentoUI" WHERE "IdTipoDocumento" IN ('CIR1985','OFI253');

/*==============================================================*/
/* Insert Table: TipoDocumento                     	                */
/*==============================================================*/


---Abarca la Nomina y El Resumen de la Circularizacion
INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaProceso")
VALUES ('CIR1985','Circular 1985','EJECUCIONORDEN','CIR1985','PROCESONOMINAEJECUCIONDEORDENES');

INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaProceso")
VALUES ('OFI253','Oficio 253','EJECUCIONORDEN','OFI253','');

/*==============================================================*/
/* Insert Table: TipoDocumentoUI          	                            */
/*==============================================================*/
INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumentoUI" ( "IdTipoDocumento","UI","IdEstado")
VALUES ('CIR1985','NOTIFICACIONORDENES','ACT');
INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumentoUI" ( "IdTipoDocumento","UI","IdEstado")
VALUES ('OFI253','NOTIFICACIONORDENES','ACT');

/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/

---Esquema Indefinido
INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('CIR1985', 'V1', 'NOR', 'M', '', '', 'SVS', '', '20160101', '99990101', 0);

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('OFI253', 'V1', 'NOR', 'M', '', '', 'SVS', '', '20160101', '99990101', 0);

/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
