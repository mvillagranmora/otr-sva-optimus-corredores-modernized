SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarDiccionario";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarDiccionario"
AS 

SELECT
    "Tipo",
    "Codigo",
    "Orden",
    "Descripcion",
    "Activo",
    "Objetivo",
    "Producto"
    
FROM "BSCL.Normativa.BD::DOC.Diccionario";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarDiccionario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
