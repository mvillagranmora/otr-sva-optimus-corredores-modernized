SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto"
AS
	SELECT
		"PER"."IdPersona",
		"PER"."IdDocumentoIdentidad",
		CASE "PER"."Tipo" 
			WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
			ELSE "PER"."RazonSocial"
		END  as "NombreCompleto"
	FROM   "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
WITH READ ONLY; 


GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
