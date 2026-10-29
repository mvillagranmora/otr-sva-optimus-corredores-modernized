SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEnrolamiento";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEnrolamiento" AS
	
SELECT 
	ENR.*,
	CIU."IdPais"
FROM  "BSCL.SocioNegocio.BD::SOC.Enrolamiento" AS ENR
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CIU
	ON (CIU."IdCiudad" = ENR."IdCiudad");
	
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarEnrolamiento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 
 