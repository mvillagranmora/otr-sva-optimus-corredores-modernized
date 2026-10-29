SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoPorConfiguracion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoPorConfiguracion"  
AS
select distinct
evt."IdEvento",
evt."IdUsuario",
evt."FechaCreacion",
evt."Procesado",
evt."TipoEntidadOrigen",
evt."IdEntidadOrigen",
evt."TipoEntidadDestino",
to_char(evt."MensajeBody") as "MensajeBody",
to_char(evt."MensajeRecordatorio") as "MensajeRecordatorio",
evt."IdEntidadDestino",
ced."IdConfiguracionEvento"
from "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento" evt
left join "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles" ced
on evt."CodigoTipoEvento" = ced."CodigoTipoEvento";




GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoPorConfiguracion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 