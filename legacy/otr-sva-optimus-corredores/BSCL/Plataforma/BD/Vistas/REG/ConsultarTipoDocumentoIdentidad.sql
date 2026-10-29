SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoDocumentoIdentidad"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoDocumentoIdentidad"  
AS
SELECT 
	"TipoDocumentoIdentidad_$0"."IdTipoDocumentoIdentidad" , 
	"TipoDocumentoIdentidad_$0"."NombreTipoDocumentoIdentidad" , 
	"TipoDocumentoIdentidad_$0"."NombreAbrTipoDocumentoIdentidad" , 
	"TipoDocumentoIdentidad_$0"."IdPais" , 
	"TipoDocumentoIdentidad_$0"."PorDefecto"
	,"TipoDocumentoIdentidad_$0"."IdEstado"
	
FROM "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS "TipoDocumentoIdentidad_$0";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::REG.ConsultarTipoDocumentoIdentidad" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
