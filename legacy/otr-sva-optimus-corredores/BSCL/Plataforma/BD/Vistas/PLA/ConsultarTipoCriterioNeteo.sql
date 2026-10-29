SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCriterioNeteo"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCriterioNeteo" as
	SELECT 
		"IdTipoCriterioNeteo",
		"NombreTipoCriterioNeteo",
		"SecuenciaNeteo",
		"Editable"
	FROM "BSCL.Plataforma.BD::PLA.TipoCriterioNeteo"
WITH READ ONLY;

GRANT SELECT on "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCriterioNeteo" to BCS_DESARROLLADOR with grant option;
