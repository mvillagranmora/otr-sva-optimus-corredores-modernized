
SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM  "BSCL"."BSCL.Plataforma.BD::PND.AsuntoPendiente";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PND.AsuntoPendiente" ("Codigo", "IdEmpresa", "Asunto", "CodigoNotificacion", "CumplimientoAutomatico", "DiasVisibleDesde", "DiasVisibleHasta", "FormatoGlosa", "IdEstado")
VALUES ('CUCLI','1','Cumpleaños de Clientes','NOTICUCLI','S','0','7','Cliente #nombre# cumple #anios# años el #fecha#, recuerda saludarlo.','VIG');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PND.AsuntoPendiente" ("Codigo", "IdEmpresa", "Asunto", "CodigoNotificacion", "CumplimientoAutomatico", "DiasVisibleDesde", "DiasVisibleHasta", "FormatoGlosa", "IdEstado")
VALUES ('DOCVENCIDOS','1','Aviso documentos vencidos','DOCVENCIDOS','N','0','5','El documento: #documento# del cliente: #cliente# está vencido con fecha: #fecha#.','VIG');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PND.AsuntoPendiente" ("Codigo", "IdEmpresa", "Asunto", "CodigoNotificacion", "CumplimientoAutomatico", "DiasVisibleDesde", "DiasVisibleHasta", "FormatoGlosa", "IdEstado")
VALUES ('DOCAVENCER','1','Aviso documentos a vencer','DOCAVENCER','N','0','5','El documento: #documento# del cliente: #cliente# vencerá en la fecha: #fecha#.','VIG');

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
