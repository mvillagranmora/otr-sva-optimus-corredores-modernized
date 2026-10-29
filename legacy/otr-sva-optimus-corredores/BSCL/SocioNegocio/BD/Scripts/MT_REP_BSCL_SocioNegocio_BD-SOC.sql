SET SCHEMA "BSCL"; 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"; 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 1,'ACT','PERSONA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 2,'BLO','PERSONA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 3,'ACT','CLIENTE'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 4,'BLO','CLIENTE'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 5,'ACT','RELACION'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 6,'INA','RELACION'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 7,'ACT','EMPLEADO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 8,'BLO','EMPLEADO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 9,'ACT','DOCUMENTOIDENTIDAD'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 11,'ACT','CUENTABANCARIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 12,'INA','CUENTABANCARIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 13,'ACT','TELEFONO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 14,'INA','TELEFONO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 15,'ACT','DIRECCIONELECTRONICA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 16,'INA','DIRECCIONELECTRONICA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 17,'ACT','DIRECCIONFISICA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 18,'INA','DIRECCIONFISICA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 19,'ACT','TARJETABANCARIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 20,'INA','TARJETABANCARIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 21,'ACT','PROVEEDOR'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 22,'BLO','PROVEEDOR'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 23,'PEN','PERSONA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 24,'PEN','CLIENTE'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 25,'PEN','RELACION'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 26,'PEN','EMPLEADO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 27,'PEN','DOCUMENTOIDENTIDAD'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 28,'PEN','CUENTABANCARIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 29,'PEN','TELEFONO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 30,'PEN','DIRECCIONELECTRONICA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 31,'PEN','DIRECCIONFISICA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 32,'PEN','TARJETABANCARIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 33,'PEN','PROVEEDOR'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 10,'VCD','DOCUMENTOIDENTIDAD'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 34,'PEN','NOMBREFANTASIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 35,'INA','NOMBREFANTASIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 36,'ACT','NOMBREFANTASIA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 37,'ACT','GRUPODIRECCIONELECTRONICA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 38,'INA','GRUPODIRECCIONELECTRONICA'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio"  ( "IdEstadoSocioDeNegocio","IdEstado","NombreEntidad") VALUES ( 39,'PEN','GRUPODIRECCIONELECTRONICA'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"; 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BNOUSO','PERSONA','BLOQUEO POR NO USO','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BMUERT','PERSONA','BLOQUEO POR MUERTE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BFRAUD','PERSONA','BLOQUEO POR FRAUDE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BNOUSO','CLIENTE','BLOQUEO POR NO USO','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BMUERT','CLIENTE','BLOQUEO POR MUERTE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BFRAUD','CLIENTE','BLOQUEO POR FRAUDE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BNOUSO','EMPLEADO','BLOQUEO POR NO USO','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BMUERT','EMPLEADO','BLOQUEO POR MUERTE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BFRAUD','EMPLEADO','BLOQUEO POR FRAUDE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BNOUSO','PROVEEDOR','BLOQUEO POR NO USO','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BMUERT','PROVEEDOR','BLOQUEO POR MUERTE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BFRAUD','PROVEEDOR','BLOQUEO POR FRAUDE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BNOUSO','CONTRAPARTE','BLOQUEO POR NO USO','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BMUERT','CONTRAPARTE','BLOQUEO POR MUERTE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BFRAUD','CONTRAPARTE','BLOQUEO POR FRAUDE','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBACTUALIZADOC','PERSONA','ACTUALIZACION DE INFORMACION','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBTEMPORAL','PERSONA','DESBLOQUEO TEMPORAL','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBACTUALIZADOC','CLIENTE','ACTUALIZACION DE INFORMACION','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBTEMPORAL','CLIENTE','DESBLOQUEO TEMPORAL','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBACTUALIZADOC','EMPLEADO','ACTUALIZACION DE INFORMACION','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBTEMPORAL','EMPLEADO','DESBLOQUEO TEMPORAL','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBACTUALIZADOC','PROVEEDOR','ACTUALIZACION DE INFORMACION','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBTEMPORAL','PROVEEDOR','DESBLOQUEO TEMPORAL','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBACTUALIZADOC','CONTRAPARTE','ACTUALIZACION DE INFORMACION','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBTEMPORAL','CONTRAPARTE','DESBLOQUEO TEMPORAL','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'BCOBRANZA','PORTAFOLIO','BLOQUEO POR COBRANZA','BLO','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBACTUALIZADOC','PORTAFOLIO','ACTUALIZACION DE INFORMACION','ACT','ACT'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio"  ( "IdEmpresa","IdTipoBloqueo","IdTipoEntidad","NombreTipoBloqueo","IdAccion","IdEstado") VALUES ( 1,'DESBTEMPORAL','PORTAFOLIO','DESBLOQUEO TEMPORAL','ACT','ACT'); 
 COMMIT; 
 END; 
 
 
DO BEGIN AUTONOMOUS TRANSACTION 
  DELETE FROM "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio"; 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio"  ( "IdTipoSocioDeNegocio","NombreTipoSocioDeNegocio") VALUES ( 'CLI','CLIENTE'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio"  ( "IdTipoSocioDeNegocio","NombreTipoSocioDeNegocio") VALUES ( 'PRO','PROVEEDOR'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio"  ( "IdTipoSocioDeNegocio","NombreTipoSocioDeNegocio") VALUES ( 'EMP','EMPLEADO'); 
   INSERT INTO "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio"  ( "IdTipoSocioDeNegocio","NombreTipoSocioDeNegocio") VALUES ( 'CNT','CONTRAPARTE'); 
 COMMIT; 
 END; 
 
 
