SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarAtributoAnotacion"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarAtributoAnotacion" AS 
SELECT  
"IdEmpresa",
"IdAtributoAnotacion",
"TipoAtributoAnotacion",
"NombreAtributoAnotacion"
from "BSCL.Portafolio.BD::POR.AtributoAnotacion";
		

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarAtributoAnotacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
