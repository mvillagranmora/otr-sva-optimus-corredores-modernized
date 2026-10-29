SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosSinValidacionFlujo";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosSinValidacionFlujo" AS
SELECT 
	PAR."Valor" AS "Conceptos"
FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarParametrosSistema" AS PAR 
WHERE PAR."CodigoParametro" = 'PLA.ConceptosCheckSinValidacionFlujoCaja';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptosSinValidacionFlujo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;