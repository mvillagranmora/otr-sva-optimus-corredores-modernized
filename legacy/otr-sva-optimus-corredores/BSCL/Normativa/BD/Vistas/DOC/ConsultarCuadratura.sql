SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarCuadratura";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarCuadratura"
AS 

SELECT
    "Esquema",
    "Version",
    "Tipo",
    "Descripcion"
FROM "BSCL.Normativa.BD::DOC.Cuadratura";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarCuadratura" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
