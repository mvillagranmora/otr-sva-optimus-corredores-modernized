SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarAnotaciones"; 
GO
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarAnotaciones" AS 
SELECT  
A."IdAnotacion",
A."IdPortafolio",
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
 from "BSCL.Portafolio.BD::POR.Anotacion" A
 LEFT JOIN "BSCL.Portafolio.BD::POR.AtributoAnotacion" MOT ON A."IdMotivoAnotacion" = MOT."IdAtributoAnotacion"
 LEFT JOIN "BSCL.Portafolio.BD::POR.AtributoAnotacion" REL ON A."IdRelevanciaAnotacion" = REL."IdAtributoAnotacion"
 LEFT JOIN "BSCL.Portafolio.BD::POR.AtributoAnotacion" AMB ON A."IdAmbitoAnotacion" = AMB."IdAtributoAnotacion"
 ;
 
GO		
GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarAnotaciones" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO