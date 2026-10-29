SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisicaAnalitica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisicaAnalitica" AS
SELECT 
	DF."Calle",
	DF."Numero",
	DF."Departamento",
	DF."Sector",
	DF."CodigoPostal",
	DF."CodigoProposito",
	DF."Comentario",
	DF."IdEstado",
	PRO."Descripcion" AS "DescripcionProposito" ,
	DF."IdDireccionFisica",
	DF."IdCiudad",
	DF."IdPersona",
	CIU."NombreCiudad", 
	DF."IdDivisionPais"
FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" DF
INNER JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" PRO ON DF."CodigoProposito" = PRO."IdPropositoMedioContacto"
INNER JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIU ON DF."IdCiudad" = CIU."IdCiudad"
WHERE DF."Borrado" = 'N';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisicaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;