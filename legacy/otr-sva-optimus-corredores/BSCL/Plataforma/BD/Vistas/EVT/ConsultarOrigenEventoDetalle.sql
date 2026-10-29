SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarOrigenEventoDetalle"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarOrigenEventoDetalle"  
AS

SELECT OrigenEvento."IdOrigenEvento", 
       Evento."IdEvento", 
       Evento."CodigoTipoEvento", 
       Evento."FechaCreacion",
       Evento."MensajeBody",
       NotificacionDetalle."IdNotificacion",
       Notificacion."IdConfiguracionEvento"
FROM "BSCL.Plataforma.BD::EVT.OrigenEvento" OrigenEvento
INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" Evento ON (Evento."IdOrigenEvento" = OrigenEvento."IdOrigenEvento")
INNER JOIN "BSCL.Plataforma.BD::EVT.NotificacionDetalle" NotificacionDetalle ON (NotificacionDetalle."IdEvento" = Evento."IdEvento")
INNER JOIN "BSCL.Plataforma.BD::EVT.Notificacion" Notificacion ON (NotificacionDetalle."IdNotificacion" = Notificacion."IdNotificacion");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarOrigenEventoDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 
