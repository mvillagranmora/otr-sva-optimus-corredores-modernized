SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAtributoAnotacion"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAtributoAnotacion" AS 
SELECT  
"IdEmpresa",
"IdAtributoAnotacion",
"TipoAtributoAnotacion",
"NombreAtributoAnotacion"
from "BSCL.Portafolio.BD::POR.AtributoAnotacion";
		

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAtributoAnotacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;