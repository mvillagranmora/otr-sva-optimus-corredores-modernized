SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacionDetalle"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacionDetalle"  
AS
select 
"IdNotificacionDetalle",
"IdNotificacion",
evt."IdEvento",
tipoevt."CodigoTipoEvento",
CASE "Despachado"  WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Despachado",
"Descripcion"
 from "BSCL.Plataforma.BD::EVT.NotificacionDetalle" notdet
 INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" evt
ON evt."IdEvento" = notdet."IdEvento"
 INNER JOIN "BSCL.Plataforma.BD::EVT.TipoEvento" tipoevt
ON evt."CodigoTipoEvento" = tipoevt."CodigoTipoEvento"; 

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacionDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

