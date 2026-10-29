SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Homologacion.BD.Vistas::TipoCarga";
CREATE VIEW  "BSCL.Homologacion.BD.Vistas::TipoCarga"( 
"IdTipoCarga"
,"Codigo"
) AS 
SELECT 
"IdTipoCarga"
,"Codigo"
FROM "BSCL.Homologacion.BD::HOM.TipoCarga"
WITH READ ONLY;	 
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HOM.TipoCarga" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
   
 
