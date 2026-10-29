SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/



--DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"='CERTCLI';
DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='CERTCLI';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='CERTCLI';

/*==============================================================*/
/* Insert Table: TipoDocumento                     	                */
/*==============================================================*/



INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion")
VALUES ('CERTCLI','Certificados cliente','CERTCLIENTE','CERTCLI','CERTIFICADOS_CLIENTES_MASIVO');


/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version", "Modulo", "Periodicidad", "Objetivo", "PeriodoInicio", "IdEntidadReguladora", "FechaPresentacionLimite", "FechaDatosDesde", "FechaDatosHasta", "CodigoVersionSII")
VALUES ('CERTCLI', 'V1', 'NOR', 'M', '', '', 'BCS', '20170110', '20170101', '0001-01-01', 2);
/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/
						
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
