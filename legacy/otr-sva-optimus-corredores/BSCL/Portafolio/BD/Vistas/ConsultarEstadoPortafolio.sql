SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarEstadoPortafolio"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarEstadoPortafolio" AS 

SELECT
	 ESTPORT."IdEstado" ,
	 EST."Nombre", 
	 ESTPORT."NombreEntidad"
FROM "BSCL.Portafolio.BD::POR.EstadoPortafolio" AS ESTPORT
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST
	ON EST."IdEstado" = ESTPORT."IdEstado";

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarEstadoPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


