SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Homologacion.BD.Vistas::ModoCarga";
CREATE VIEW  "BSCL.Homologacion.BD.Vistas::ModoCarga"( 
"IdModoCarga"
,"Codigo"
) AS 
SELECT 
"IdModoCarga"
,"Codigo"
FROM "BSCL.Homologacion.BD::HOM.ModoCarga"
WITH READ ONLY;
GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::ModoCarga" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
   
