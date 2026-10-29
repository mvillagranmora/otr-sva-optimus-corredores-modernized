SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio" AS 
SELECT   
	P.*
FROM 
	"BSCL.Portafolio.BD::POR.PropositoPortafolio" AS P;

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPropositoPortafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
