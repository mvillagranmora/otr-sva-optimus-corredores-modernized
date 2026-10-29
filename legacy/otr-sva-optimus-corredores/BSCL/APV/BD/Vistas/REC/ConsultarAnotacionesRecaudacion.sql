SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarAnotacionesRecaudacion"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarAnotacionesRecaudacion" AS 
SELECT  
A."IdAnotacion",
A."IdRecaudacion",
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
from "BSCL.APV.BD::REC.Anotacion" A 
JOIN "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion" MOT ON A."IdMotivoAnotacion" = MOT."IdAtributoAnotacion"
JOIN "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion" REL ON A."IdRelevanciaAnotacion" = REL."IdAtributoAnotacion"
JOIN "BSCL.SocioNegocio.BD::CLI.AtributoAnotacion" AMB ON A."IdAmbitoAnotacion" = AMB."IdAtributoAnotacion"
;
		
GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarAnotacionesRecaudacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 