SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarClasificacionCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarClasificacionCobro"  
AS

SELECT 
	CC."IdCobroClasificacion",
	CC."CodigoCobroClasificacion"
	
FROM "BSCL.Plataforma.BD::COM.CobroClasificacion" as CC;
	

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarClasificacionCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
