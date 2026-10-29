SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona"  AS 
SELECT 
	"TPE"."IdTipoPersona", 
	"TPE"."Descripcion" 
FROM 
"BSCL.SocioNegocio.BD::PER.TipoPersona" AS "TPE";
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
