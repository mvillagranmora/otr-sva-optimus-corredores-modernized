SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultarClaseDocumento"; 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::DOC.ConsultarClaseDocumento"  AS 
SELECT 
	"CDO"."IdClase", 
	"CDO"."Descripcion" 
FROM 
"BSCL.Plataforma.BD::DOC.ClaseDocumento" AS "CDO"; 
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::DOC.ConsultarClaseDocumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
