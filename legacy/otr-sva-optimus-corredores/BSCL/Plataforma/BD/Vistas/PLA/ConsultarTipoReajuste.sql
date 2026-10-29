SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoReajuste"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoReajuste"  
AS
SELECT 
	tiporeaj."IdTipoReajuste" , 
	tiporeaj."Codigo" , 
	tiporeaj."Nombre" , 
	tiporeaj."Reajustable" , 
	tiporeaj."IdEstado" ,  
	((tiporeaj."IdTipoReajuste" || ' - ') || tiporeaj."Nombre")  AS "NombreLargo" 
FROM "BSCL.Plataforma.BD::PLA.TipoReajuste" AS tiporeaj;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoReajuste" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
