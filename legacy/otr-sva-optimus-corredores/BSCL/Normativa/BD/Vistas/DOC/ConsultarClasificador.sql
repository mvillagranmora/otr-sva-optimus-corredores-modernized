SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarClasificador";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarClasificador"
AS 

SELECT
    "Esquema",
    "Version",
    "Tipo",
    "Descripcion"
FROM "BSCL.Normativa.BD::DOC.Clasificador";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarClasificador" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
