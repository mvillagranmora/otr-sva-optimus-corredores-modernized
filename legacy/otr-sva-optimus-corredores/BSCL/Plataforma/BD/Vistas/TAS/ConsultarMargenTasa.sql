SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarMargenTasa";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::TAS.ConsultarMargenTasa" AS
SELECT
	 MTA."IdMargenTasa"
	,MTA."TipoEntidad"
	,MTA."IdMoneda"
	,MTA."MontoDesde"
	,MTA."MontoHasta"
	,MTA."PlazoDesde"
    ,MTA."PlazoHasta"
	,MTA."Tasa"
    ,MTA."IdEstado"
    ,EST."Descripcion" AS "Estado"
	,MTA."IdEmpresa"
    ,MTA."FechaIngreso"
    ,MTA."FechaCreacion"
    ,MTA."FechaModificacion"
	,MTA."IdUsuarioCreador"
    ,MTA."Borrado"
FROM "BSCL.Plataforma.BD::TAS.MargenTasa" MTA
INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarEstadoTasa" EST
	ON (MTA."IdEstado" = EST."IdEstado" AND EST."NombreEntidad" = 'MANTENEDORMARGENESTASAS')
WHERE MTA."Borrado" = 'N'
ORDER BY MTA."IdMargenTasa" ASC, MTA."TipoEntidad", MTA."IdMoneda";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TAS.ConsultarMargenTasa" TO BCS_DESARROLLADOR WITH GRANT OPTION;