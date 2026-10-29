SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento"  
AS

select
"IdEmpresa",
"IdEvento",
"CodigoTipoEvento",
"IdUsuario",
"FechaCreacion",
"Procesado",
"TipoEntidadOrigen",
"IdEntidadOrigen",
"TipoEntidadDestino",
"MensajeBody",
"MensajeRecordatorio",
"IdEntidadDestino",
"IdPersonaRemitente",
"IdPersonaDestinatario"

 from "BSCL.Plataforma.BD::EVT.Evento";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEvento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 