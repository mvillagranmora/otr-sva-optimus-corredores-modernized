SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarParametroFinanciero"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::MER.ConsultarParametroFinanciero" AS
SELECT 	
"IdParametroFinanciero",
"IdParametroFinanciero" AS "NombreParametroFinanciero",
"Fecha",
"AgnoMes",
"Agno",
"Valor",
"FechaActualizacion"
FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero";
			
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::MER.ConsultarParametroFinanciero" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
