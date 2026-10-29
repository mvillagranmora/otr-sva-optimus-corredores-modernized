SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarModeloCuentaCustodio"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarModeloCuentaCustodio" AS 

SELECT "IdModeloCustodia","NombreModeloCustodia" FROM "BSCL.Portafolio.BD::POR.ModeloCustodia";

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarModeloCuentaCustodio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


