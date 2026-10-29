SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoGenero"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoGenero"  AS 
SELECT 
	"TGE"."IdTipoGenero", 
	"TGE"."Descripcion" 
FROM 
"BSCL.SocioNegocio.BD::PER.TipoGenero" AS "TGE";
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoGenero" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
