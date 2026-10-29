SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensajeExcepcionDestinatario";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensajeExcepcionDestinatario" AS
SELECT
"IdMensaje",
"IdPersonaDestinatario"
FROM "BSCL.Plataforma.BD::EVT.MensajeExcepcionDestinatario";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarMensajeExcepcionDestinatario" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

 
