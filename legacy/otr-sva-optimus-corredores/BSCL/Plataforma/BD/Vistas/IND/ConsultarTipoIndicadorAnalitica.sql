SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicadorAnalitica";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicadorAnalitica" AS
SELECT
	"IdEmpresa",
	"IdTipoIndicador",
	CASE WHEN "IdTipoEntidad" = '' THEN NULL ELSE CAST("IdTipoEntidad" AS NVARCHAR(40)) END AS "IdTipoEntidad",
	CAST("NombreTipoIndicador" AS NVARCHAR(255)) AS "NombreTipoIndicador",
	CAST("CodigoTipoIndicador" AS NVARCHAR(40)) AS "CodigoTipoIndicador",
	CAST("ValorIndicadorDefecto" AS NVARCHAR(255)) AS "ValorIndicadorDefecto",
	"CantidadDecimales",
	"LongitudMinima",
	"LongitudMaxima",
	CASE WHEN "IdUso" = '' THEN NULL ELSE "IdUso" END AS "IdUso",
	CASE WHEN "IdEstado" = '' THEN NULL ELSE "IdEstado" END AS "IdEstado",
	"IdTipoPersona"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicadorAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
