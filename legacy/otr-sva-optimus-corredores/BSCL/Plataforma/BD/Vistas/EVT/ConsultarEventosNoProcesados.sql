SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventosNoProcesados"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventosNoProcesados"  
AS
select evt."IdEvento", evt."Procesado", conf."Agrupa" ,confEVT."IdConfiguracionEvento", evt."TipoEntidadDestino", confEVT."IdEstado" as "IdEstadoDetalle" ,conf."IdEstado",evt."IdPersonaRemitente", evt."IdPersonaDestinatario", evt."IdPortafolio", conf."PermitePersonalizar"
from "BSCL.Plataforma.BD::EVT.Evento" evt
LEFT JOIN "BSCL.Plataforma.BD::EVT.TipoEvento" tipoevt on tipoevt."CodigoTipoEvento" = evt."CodigoTipoEvento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" confEVT ON confEVT."CodigoTipoEvento" = tipoevt."CodigoTipoEvento"
LEFT OUTER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" conf ON confEVT."IdConfiguracionEvento" =   conf."IdConfiguracionEvento";
    
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventosNoProcesados" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

