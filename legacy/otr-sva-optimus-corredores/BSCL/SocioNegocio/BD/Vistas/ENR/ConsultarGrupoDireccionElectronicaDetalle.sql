SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarGrupoDireccionElectronicaDetalle";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarGrupoDireccionElectronicaDetalle" AS
	SELECT
		GDIE."IdGrupoDireccionDetalle",
		GDIE."IdDireccionElectronica",
		DIE."Direccion",
		GDIE."IdTipoDestinatario",
		TD."Nombre" as "NombreTipoDestinatario",
		GDIE."IdGrupoDireccion"
	FROM "BSCL.SocioNegocio.BD::ENR.GrupoDireccionDetalle" GDIE	
	LEFT JOIN "BSCL.SocioNegocio.BD::ENR.DireccionElectronica" DIE ON DIE."IdDireccionElectronica" = GDIE."IdDireccionElectronica"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoDestinatarioCorreo" TD ON TD."IdTipoDestinatario" = GDIE."IdTipoDestinatario"
WITH READ ONLY;

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarGrupoDireccionElectronicaDetalle" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 