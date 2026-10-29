SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarDireccionFisica";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarDireccionFisica" AS
SELECT
	DIR."IdDireccionFisica",
	DIR."Calle",
	DIR."Numero",	
	DIR."Departamento",
	DIR."Sector",
	DIR."CodigoPostal",
	DIR."IdCiudad",
	CIU."NombreCiudad",
	DIR."CodigoProposito",
	COALESCE(PRST."Descripcion",'') AS  "DescripcionProposito" , 
	DIR."Comentario",
	DIR."IdEnrolamiento",
	DIR."Predeterminado",
	DIR."CodigoCompuesto",
	PAI."IdPais",
	PAI."NombrePais",
	CASE PAI."UsaDivisionPais" WHEN 'S' THEN COALESCE(PAI."NombreDivisionPais",'') ELSE '-' END as "PaisNombreDivisionPais",
	CASE PAI."UsaDivisionPais" WHEN 'S' THEN COALESCE(DIVPAI."IdDivisionPais",0) ELSE 0 END AS "IdDivisionPais",
	CASE PAI."UsaDivisionPais" WHEN 'S' THEN COALESCE(DIVPAI."NombreDivisionPais",'-') ELSE '-' END AS "NombreDivisionPais",
	COALESCE(DIVPAI."Codigo",'') as "CodigoDivisionPais"
FROM "BSCL.SocioNegocio.BD::ENR.DireccionFisica" AS DIR
LEFT JOIN "BSCL.SocioNegocio.BD::PER.PropositoMedioContacto" PRST ON PRST."IdPropositoMedioContacto" = DIR."CodigoProposito"
LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIU ON CIU."IdCiudad" = DIR."IdCiudad"
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DIVPAIS ON DIVPAIS."IdDivisionPais" = DIR."IdDivisionPais"
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" as PAI on CIU."IdPais" = PAI."IdPais"
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS DIVPAI on (DIR."IdDivisionPais" = DIVPAI."IdDivisionPais");

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarDireccionFisica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
