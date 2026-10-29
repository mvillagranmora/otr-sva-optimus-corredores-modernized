SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" AS
SELECT
	 FU."IdFuente"
	,FU."Codigo"
	,FU."Descripcion"
	,FU."Codigo"||' - '||FU."Descripcion" AS "CodigoDescripcion"
	,FU."IdEmpresa"
	,FU."IdEstado"
	,FU."NombreEntidad"
FROM "BSCL.Plataforma.BD::TAS.Fuente" FU;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" TO BCS_DESARROLLADOR WITH GRANT OPTION;
