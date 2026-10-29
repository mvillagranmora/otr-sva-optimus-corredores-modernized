SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarTipoCuentaCustodia"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarTipoCuentaCustodia" AS 

SELECT "IdTipoCuentaCustodia","NombreTipoCuentaCustodia" FROM "BSCL.Portafolio.BD::POR.TipoCuentaCustodia";

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarTipoCuentaCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


