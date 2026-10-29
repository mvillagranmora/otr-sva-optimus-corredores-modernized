SET SCHEMA "BSCL"; 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::REL.NormativaRelacion"; 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacion"  ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion") VALUES ( 1,'CIR1237','GG','ACT','GERENTE GENERAL'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacion"  ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion") VALUES ( 2,'CIR1237','GA','ACT','GERENTE DE AREA O SUBGERENTE'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacion"  ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion") VALUES ( 3,'CIR1237','JM','ACT','JEFE DE MESA DE DINERO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacion"  ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion") VALUES ( 4,'CIR1237','OM','ACT','OPERADOR DE MESA DE DINERO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacion"  ( "IdNormativaRelacion","IdNormativa","IdTipoRelacion","IdEstado","DescripcionRelacion") VALUES ( 5,'CIR1237','OTR','ACT','OTRA RELACION'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"; 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 1,'NV','NR'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 2,'VP','C-PRO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 3,'VL','GA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 4,'VL','OP'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 5,'VL','APO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 6,'VL','CY'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 7,'VL','APO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 8,'SOC','C-SOC'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 9,'DIR','C-DIR'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 10,'TR','C-EM'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 11,'TR','C-GG'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 12,'TR','C-GA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 13,'TR','C-JM'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 14,'TR','C-OM'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 15,'OTR','OTR'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.NormativaRelacionClasificacion"  ( "IdNormativaRelacionClasificacion","IdClasificacionRelacion","IdTipoRelacion") VALUES ( 16,'SR','NR'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"; 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"  ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado") VALUES ( 'SOC','SOCIO','NCG380','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"  ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado") VALUES ( 'DIR','DIRECTOR','NCG380','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"  ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado") VALUES ( 'TR','TRABAJADOR','NCG380','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"  ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado") VALUES ( 'MG','MIEMBRO GRUPO EMPRESARIAL','NCG380','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"  ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado") VALUES ( 'OTR','OTRA RELACION','NCG380','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"  ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado") VALUES ( 'SR','SIN RELACION','NCG380','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"  ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado") VALUES ( 'NV','NO VINCULADO AL INTERMEDIARIO','CIR1064','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"  ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado") VALUES ( 'VP','VINCULADO POR PROPIEDAD AL INTERMEDIARIO','CIR1064','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoClasificacionRelacion"  ( "IdClasificacionRelacion","NombreClasificacion","IdNormativa","IdEstado") VALUES ( 'VL','VINCULADO LABORALMENTE AL INTERMEDIARIO','CIR1064','ACT'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion"; 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'APO',1,'S','','APODERADO','','N','ACT','RELACIONADOS','N','S'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'ATAPO',1,'S','PERSONA','AUTORIZA APODERADO','APO','N','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'VINPRO',1,'S','CLIENTE','VINCULADO POR PROPIEDAD AL INTERMEDIARIO','','N','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'VINLAB',1,'S','EMPLEADO','?VINCULADO LABORALMENTE AL INTERMEDIARIO','','N','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'VINADM',1,'S','CLIENTE','?VINCULADO POR ADMINISTRACION AL INTERMEDIARIO','','N','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'ENTGEM',1,'S','PERSONA','ENTIDAD DEL GRUPO EMPRESARIAL','','N','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'MIADMS',1,'S','PERSONA','MIEMBRO DE LA ADMINISTRACION CONTROLE >=10% SOCIEDAD','','N','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'SOC',1,'S','','SOCIO','','N','ACT','RELACIONADOS','S','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'REPAD',1,'S','PERSONA','REPRESENTANTE LEGAL ADICIONAL','','N','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'CY',1,'S','PERSONA','CONYUGE','','S','ACT','PERSONA','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'REP',1,'S','PERSONA','REPRESENTANTE LEGAL','','S','ACT','PERSONA','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'CEMDOR',1,'N','CORREDOR','EMPLEADOR','','S','ACT','EMPLEADO','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'PEMDOR',1,'N','PERSONA','EMPLEADOR','','S','ACT','EMPLEADO','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'AUTZDO',1,'S','PERSONA','AUTORIZADO A DAR ORDENES','AUTDOR','S','ACT','AUTORIZADO','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'AUTDOR',1,'S','PERSONA','AUTORIZA A DAR ORDENES','','S','ACT','AUTORIZADO','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'OTRARE',1,'S','PERSONA','OTRA RELACION (SISTEMA)','','S','ACT','SISTEMA','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'SINREL',1,'S','PERSONA','SIN RELACION','','S','ACT','SISTEMA','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'PARSOC',1,'S','CLIENTE','PARTICIPACION EN SOCIEDADES','','S','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'APOFIR',1,'S','CLIENTE','APODERADO FIRMANTE','ATAPOF','N','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'ATAPOF',1,'S','PERSONA','AUTORIZA APODERADO FIRMANTE','APOFIR','N','ACT','RELACIONADOS','N','N'); 
   INSERT INTO "BSCL.SocioNegocio.BD::REL.TipoRelacion"  ( "IdTipoRelacion","IdEmpresa","EsPersonaNatural","IdTipoEntidadOrigen","NombreTipoRelacion","IdTipoRelacionDestino","EsIndefinida","IdEstado","Administracion","UsaPorcentajeParticipacion","RetiraDocumentos") VALUES ( 'BENEMP',1,'S','','BENEFICIARIO DE LA EMPRESA','','S','ACT','RELACIONADOS','S','N'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::REL.TipoRelacionDestino"; 
 COMMIT; 
 END; 
 
 
