SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPropositoMedioContacto"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPropositoMedioContacto"  AS 
SELECT 
	"TPR"."IdPropositoMedioContacto", 
	"TPR"."Descripcion" 
FROM 
"BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" AS "TPR";
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPropositoMedioContacto" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
