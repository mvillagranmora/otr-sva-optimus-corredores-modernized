SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupadorNOAPV"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupadorNOAPV" AS 

SELECT 
NOAPV."IdMatrizAgrupadorNOAPV"
,NOAPV."CodigoPortafolio"
,NOAPV."EsADC"
FROM "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" NOAPV;

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupadorNOAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;