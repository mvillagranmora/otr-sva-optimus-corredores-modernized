SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDestinatarios"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDestinatarios"  
AS

SELECT 
	Evento."IdEvento", DireccionElectronica."Direccion", "Nombre" AS "TipoDestinatario",

	OrigenEvento."IdOrigenEvento",
	Evento."MensajeBody"
 
FROM "BSCL.Plataforma.BD::EVT.OrigenEvento" OrigenEvento
INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" Evento ON (Evento."IdOrigenEvento" = OrigenEvento."IdOrigenEvento")
INNER JOIN "BSCL.Plataforma.BD::EVT.EventoDirecciones" EventoDirecciones ON (EventoDirecciones."IdEvento" = Evento."IdEvento")
INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DireccionElectronica ON (DireccionElectronica."IdDireccionElectronica" = EventoDirecciones."IdDireccionElectronica")
INNER JOIN "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" TipoDestinatarioCorreo ON (TipoDestinatarioCorreo."IdTipoDestinatario" = EventoDirecciones."TipoDestinatario");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarEventoDestinatarios" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 
