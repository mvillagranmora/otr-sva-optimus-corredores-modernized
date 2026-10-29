SET SCHEMA "BSCL"; 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Portafolio.BD::POR.AtributoAnotacion"; 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'ENVCAR','MOTIVO','ENVIO CARTOLA'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'EVTBUR','MOTIVO','EVENTO BURSATIL'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'AVICOM','MOTIVO','AVISOS COMERCIALES'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'RECORD','MOTIVO','RECORDATORIO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'OTROS','MOTIVO','OTROS'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'ALTA','RELEVANCIA','ALTA'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'NORMAL','RELEVANCIA','NORMAL'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'PUBLIC','AMBITO','PUBLICO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'PRIVAD','AMBITO','PRIVADO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.AtributoAnotacion"  ( "IdEmpresa","IdAtributoAnotacion","TipoAtributoAnotacion","NombreAtributoAnotacion") VALUES ( 1,'FILE','MOTIVO','ARCHIVO ADJUNTO'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Portafolio.BD::POR.EstadoPortafolio"; 
   INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio"  ( "IdEstadoPortafolio","IdEstado","NombreEntidad") VALUES ( 1,'ACT','PORTAFOLIO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio"  ( "IdEstadoPortafolio","IdEstado","NombreEntidad") VALUES ( 2,'BLO','PORTAFOLIO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio"  ( "IdEstadoPortafolio","IdEstado","NombreEntidad") VALUES ( 3,'ACT','ORDENAUTORIZADO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio"  ( "IdEstadoPortafolio","IdEstado","NombreEntidad") VALUES ( 4,'BLO','ORDENAUTORIZADO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio"  ( "IdEstadoPortafolio","IdEstado","NombreEntidad") VALUES ( 5,'PEN','PORTAFOLIO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio"  ( "IdEstadoPortafolio","IdEstado","NombreEntidad") VALUES ( 6,'ACT','PREFERENCIAPAGO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.EstadoPortafolio"  ( "IdEstadoPortafolio","IdEstado","NombreEntidad") VALUES ( 7,'INA','PREFERENCIAPAGO'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Portafolio.BD::POR.PortafolioClienteUsoMedioContacto"; 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Portafolio.BD::POR.PropositoPortafolio"; 
   INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio"  ( "IdPropositoPortafolio","Descripcion","IdEstado") VALUES ( 'INTERMEDIACION','INTERMEDIACION','ACT'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio"  ( "IdPropositoPortafolio","Descripcion","IdEstado") VALUES ( '57BIS','57BIS','ACT'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio"  ( "IdPropositoPortafolio","Descripcion","IdEstado") VALUES ( 'APV','APV','ACT'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio"  ( "IdPropositoPortafolio","Descripcion","IdEstado") VALUES ( 'FFMM','FONDOS MUTUOS AAA','ACT'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio"  ( "IdPropositoPortafolio","Descripcion","IdEstado") VALUES ( 'PROPGENERALES','PROPÓSITOS GENRALES','ACT'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio"  ( "IdPropositoPortafolio","Descripcion","IdEstado") VALUES ( 'ADMINCARTERA','ADMINISTRACIÓN DE CARTERA','ACT'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Portafolio.BD::POR.TipoAutorizado"; 
   INSERT INTO "BSCL.Portafolio.BD::POR.TipoAutorizado"  ( "IdEmpresa","IdTipoAutorizado","NombreTipoAutorizado","IdTipoRelacion") VALUES ( 1,'ORDEN-CREAR','CREAR ORDENES','AUTZDO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.TipoAutorizado"  ( "IdEmpresa","IdTipoAutorizado","NombreTipoAutorizado","IdTipoRelacion") VALUES ( 1,'ORDEN-CONSULTAR','CONSULTAR ORDENES','AUTZDO'); 
   INSERT INTO "BSCL.Portafolio.BD::POR.TipoAutorizado"  ( "IdEmpresa","IdTipoAutorizado","NombreTipoAutorizado","IdTipoRelacion") VALUES ( 1,'ORDEN-FIRMAR','FIRMAR ORDENES','AUTZDO'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.Portafolio.BD::POR.TipoBloqueoPortafolio"; 
 COMMIT; 
 END; 
 
 
