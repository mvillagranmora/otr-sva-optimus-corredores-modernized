SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoCriterioTasa";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoCriterioTasa" AS
SELECT
	 TCT."IdTipoCriterioTasa"
	,TCT."Codigo"
	,TCT."Descripcion"
	,TCT."IdEmpresa"
	,TCT."IdEstado"
FROM "BSCL.Plataforma.BD::TAS.TipoCriterioTasa" TCT;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoCriterioTasa" TO BCS_DESARROLLADOR WITH GRANT OPTION;
