SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarPeriodoTributarioCalificaciones";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.ConsultarPeriodoTributarioCalificaciones" (
"PeriodoComercial"
)
AS 
SELECT DISTINCT ("Ejercicio") AS "Periodo comercial"
FROM "BSCL.Normativa.BD.Vistas::TRI.ConsultarCalificacionTributaria"
ORDER BY 1 DESC
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.ConsultarPeriodoTributarioCalificaciones" TO bcs_desarrollador WITH GRANT OPTION; 