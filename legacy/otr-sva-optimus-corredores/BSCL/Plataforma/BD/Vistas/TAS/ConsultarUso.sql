SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarUso";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarUso" AS
SELECT
	 USO."IdUso"
	,USO."Codigo"
	,USO."Descripcion"
	,USO."IdEmpresa"
	,USO."IdEstado"
FROM "BSCL.Plataforma.BD::TAS.Uso" USO;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarUso" TO BCS_DESARROLLADOR WITH GRANT OPTION;
