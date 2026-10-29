SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio"  
AS
SELECT
	*
FROM 
	 "BSCL.Plataforma.BD::PLA.SolicitudCambio";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSolicitudCambio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
