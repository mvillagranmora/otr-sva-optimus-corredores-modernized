SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoMedioContacto"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoMedioContacto"  AS 
SELECT 
	"TME"."IdTipoMedioContacto", 
	"TME"."Descripcion",
	"TME"."SubTipo" 
FROM 
"BSCL.SocioNegocio.BD::PER.TipoMedioContacto" AS "TME";
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoMedioContacto" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
