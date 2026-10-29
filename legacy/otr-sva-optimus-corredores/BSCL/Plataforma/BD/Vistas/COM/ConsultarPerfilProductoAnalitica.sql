SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductoAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductoAnalitica" AS
SELECT
	pp."IdPerfilProductos",
	pp."DescripcionPerfilProductos",
	pp."IdCategoriaInversionista",
	pp."IdEstado",
	dpp."IdTipoProducto",
	dpp."IdTipoOperacion",
	dpp."IdCanalIngreso",
	dpp."IdMoneda",
	TO_NVARCHAR(dpp."FechaIngreso",'YYYY-MM-DD HH24:MI:SS') AS "FechaIngreso",
	TO_NVARCHAR(dpp."FechaCreacion",'YYYY-MM-DD HH24:MI:SS') AS "FechaCreacion",
	TO_NVARCHAR(dpp."FechaModificacion",'YYYY-MM-DD HH24:MI:SS') AS "FechaModificacion",
	dpp."IdDetallePerfilProductos",
	dpp."IdEmpresa"
FROM "BSCL.Plataforma.BD::COM.PerfilProductos" pp
INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" dpp ON pp."IdPerfilProductos" = dpp."IdPerfilProductos"
WHERE pp."EsPerfilPersonalizado" = 'N';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilProductoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 