SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoDestinatarioCorreo"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoDestinatarioCorreo"  
AS
SELECT
"IdTipoDestinatario",
"Nombre",
"IdEstado"
FROM "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoDestinatarioCorreo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 