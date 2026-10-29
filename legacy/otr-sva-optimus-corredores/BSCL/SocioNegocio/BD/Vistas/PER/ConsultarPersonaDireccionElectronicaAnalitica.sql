SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaAnalitica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaAnalitica" AS 
SELECT
	de."IdDireccionElectronica",
	de."Direccion",
	de."CodigoProposito",
	de."Comentario",
	de."IdEstado",
	de."IdPersona",
	de."IdUsuarioCreador",
	pr."Descripcion" AS "DescripcionProposito",
	de."Predeterminado"

FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" de
INNER JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" pr ON pr."IdPropositoMedioContacto" = de."CodigoProposito"
WHERE de."Borrado" = 'N';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
