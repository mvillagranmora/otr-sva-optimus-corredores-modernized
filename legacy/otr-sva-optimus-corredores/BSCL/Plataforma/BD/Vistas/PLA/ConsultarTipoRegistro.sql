SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoRegistro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoRegistro"  
AS
SELECT 	"TipoRegistro"."IdTipoRegistro" ,
		"TipoRegistro"."Descripcion"		
		
FROM "BSCL.Plataforma.BD::PLA.TipoRegistro" AS "TipoRegistro";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoRegistro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

