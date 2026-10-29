SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntos"  
AS
SELECT
       "Codigo",
       UPPER("Asunto") as "Asunto",
       "FormatoGlosa"
FROM "BSCL.Plataforma.BD::PND.AsuntoPendiente";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PND.ConsultarAsuntos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
