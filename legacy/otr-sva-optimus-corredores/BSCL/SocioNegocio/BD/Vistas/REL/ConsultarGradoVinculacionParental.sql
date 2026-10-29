SET SCHEMA "BSCL";
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarGradoVinculacionParental";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarGradoVinculacionParental" AS 
	SELECT
		"IdGradoVinculacionParental",
		"Nombre"
	FROM "BSCL.SocioNegocio.BD::REL.GradoVinculacionParental" WITH READ ONLY;
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarGradoVinculacionParental" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
