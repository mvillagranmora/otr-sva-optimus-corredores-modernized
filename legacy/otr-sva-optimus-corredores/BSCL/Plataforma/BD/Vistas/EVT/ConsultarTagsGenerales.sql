SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsGenerales"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsGenerales"  
AS
SELECT 
"IdTagGenerales",   
"Llave"		,	
"ValorDefault"
FROM "BSCL.Plataforma.BD::EVT.TagGenerales" ;
 GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTagsGenerales" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 
