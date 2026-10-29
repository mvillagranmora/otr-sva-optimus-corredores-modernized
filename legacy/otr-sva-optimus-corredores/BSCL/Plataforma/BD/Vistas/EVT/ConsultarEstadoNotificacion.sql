SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEstadoNotificacion"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEstadoNotificacion"  
AS
 select 'S' as "Enviado", 'ENVIADO' as "EnviadoUI" from dummy
  UNION ALL 
 select 'N' as "Enviado", 'PENDIENTE' as "EnviadoUI" from dummy;
 
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEstadoNotificacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


