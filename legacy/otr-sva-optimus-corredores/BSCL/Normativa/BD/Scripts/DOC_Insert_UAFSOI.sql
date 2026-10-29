SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/



--DELETE FROM "BSCL.Normativa.BD::DOC.Documento" WHERE "Esquema"='UAFROE';
--DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='UAFROE';
--DELETE FROM "BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema"='UAFROE';
DELETE FROM "BSCL.Normativa.BD::DOC.TipoDocumento" WHERE "IdTipoDocumento"='UAFSOIEST';

/*==============================================================*/
/* Insert Table: TipoDocumento                     	                */
/*==============================================================*/



INSERT INTO "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento","Descripcion","IdNormativa","Esquema","IdTipoProgramaExportacion","UI_Emision")
VALUES ('UAFSOIEST','Estadistica Operaciones del Cliente','UAF','','','N');


/*==============================================================*/
/* Insert Table: Esquema                     	                */
/*==============================================================*/


/*==============================================================*/
/* Insert Table: Campo          	                            */
/*==============================================================*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
