SET SCHEMA "BSCL";

DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFormatoDeArchivo";
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarFormatoDeArchivo" AS
SELECT 'TXT' AS "FormatoArchivo" FROM DUMMY 
UNION ALL 
SELECT 'PDF' AS "FormatoArchivo" FROM DUMMY;

GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarFormatoDeArchivo" TO BCS_DESARROLLADOR WITH GRANT OPTION;