SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarOrigenCalificacionTributaria";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarOrigenCalificacionTributaria" (
"IdOrigen"
,"Descripcion"
,"Activo"
,"Orden"
)
AS 
SELECT DICC."Codigo" AS "IdOrigen", UPPER(DICC."Descripcion"), DICC."Activo", DICC."Orden"
FROM "BSCL.Normativa.BD::DOC.Diccionario" DICC
WHERE "Tipo" = 'OrigenCalificacion'
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarOrigenCalificacionTributaria" TO bcs_desarrollador WITH GRANT OPTION;

