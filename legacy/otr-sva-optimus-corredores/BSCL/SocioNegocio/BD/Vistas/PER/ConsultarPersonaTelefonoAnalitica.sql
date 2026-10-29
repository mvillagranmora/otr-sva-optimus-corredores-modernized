SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefonoAnalitica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefonoAnalitica" AS
SELECT
	t."IdTelefono",
	t."IdPais",
	t."Numero",
	t."Anexo",
	t."CodigoProposito",
	t."Comentario",
	t."IdEstado",
	t."IdPersona",
	tt."Nombre" AS "NombreTipoTelefono",
	t."IdTipoTelefono",
	pm."Descripcion" AS "DescripcionProposito"
FROM "BSCL.SocioNegocio.BD::PER.Telefono" t
INNER JOIN "BSCL.SocioNegocio.BD::PER.TipoTelefono" tt ON t."IdTipoTelefono" = tt."IdTipoTelefono"
INNER JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" pm ON t."CodigoProposito" = pm."IdPropositoMedioContacto"
WHERE t."Borrado" = 'N';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefonoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
