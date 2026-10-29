SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarGrupoDireccionElectronica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarGrupoDireccionElectronica" AS
	SELECT
		"IdGrupoDireccion",
		"Grupo",
		"Comentario",
		"IdEnrolamiento"
	FROM "BSCL.SocioNegocio.BD::ENR.GrupoDireccion"
WITH READ ONLY;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarGrupoDireccionElectronica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 