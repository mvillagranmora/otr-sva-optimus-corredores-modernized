SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionFatca"; 

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionFatca" AS
	SELECT 
		"IdClasificacionFatca",
		"Nombre",
		"PorDefecto"
 	FROM "BSCL.SocioNegocio.BD::PER.ClasificacionFatca"
	WITH READ ONLY; 

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarClasificacionFatca" TO  BCS_DESARROLLADOR WITH GRANT OPTION;