SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoPrecio";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoPrecio" AS
SELECT
	 TPRE."IdTipoPrecio"
	,TPRE."Codigo"
	,TPRE."Descripcion"
	,TPRE."IdEmpresa"
	,TPRE."IdEstado"
	,TPRE."TipoRegistro"
FROM "BSCL.Plataforma.BD::TAS.TipoPrecio" TPRE;
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoPrecio" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO