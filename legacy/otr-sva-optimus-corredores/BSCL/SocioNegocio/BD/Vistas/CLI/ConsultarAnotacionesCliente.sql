SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnotacionesCliente"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnotacionesCliente" AS 
SELECT  
A."IdAnotacion",
A."IdCliente",
MOT."IdAtributoAnotacion" as "IdMotivo",
MOT."NombreAtributoAnotacion" as "NombreMotivo",
REL."IdAtributoAnotacion" as "IdRelevancia",
REL."NombreAtributoAnotacion" as "NombreRelevancia",
AMB."IdAtributoAnotacion" as "IdAmbito",
AMB."NombreAtributoAnotacion" as "NombreAmbito",
A."FechaAnotacion",
A."Titulo",
A."Detalle",
A."FechaIngreso",
A."FechaCreacion",
A."FechaModificacion",
A."IdUsuarioCreador",
A."Borrado",
A."NombreArchivo",
CASE COALESCE(A."NombreArchivo",'') WHEN '' THEN 'N' ELSE 'S' END "ExisteArchivo",
A."ValidoDesde",
A."ValidoHasta"
from "BSCL.SocioNegocio.BD::CLI.Anotacion" A 
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion" MOT ON A."IdMotivoAnotacion" = MOT."IdAtributoAnotacion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion" REL ON A."IdRelevanciaAnotacion" = REL."IdAtributoAnotacion"
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion" AMB ON A."IdAmbitoAnotacion" = AMB."IdAtributoAnotacion";
		
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnotacionesCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
