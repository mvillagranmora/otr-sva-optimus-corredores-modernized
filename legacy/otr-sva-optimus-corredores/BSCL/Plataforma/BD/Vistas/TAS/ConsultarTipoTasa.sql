SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa" AS
SELECT
	 TTAS."IdTipoTasa"
	,TTAS."Codigo"
	,TTAS."Descripcion"
	,TTAS."IdEmpresa"
	,TTAS."IdEstado"
	,TTAS."TipoRegistro"
FROM "BSCL.Plataforma.BD::TAS.TipoTasa" TTAS;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa" TO BCS_DESARROLLADOR WITH GRANT OPTION;
