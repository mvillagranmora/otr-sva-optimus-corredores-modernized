SELECT por."IdPortafolio",por."IdPerfilProductosExtra" AS "IdPerfilProductos",pp."IdEstado" AS "IdEstadoPerfilProducto",det."IdEstado" AS "IdEstadoDetallePerfilProducto",
		det."IdTipoProducto",det."IdTipoOperacion",det."IdCanalIngreso" as "IdTipoCanalIngreso",pp."EsPerfilPersonalizado",det."PermiteRuteo"
FROM "BSCL.Portafolio.BD::POR.Portafolio" por
INNER JOIN "BSCL.Plataforma.BD::COM.PerfilProductos" pp on por."IdPerfilProductosExtra" = pp."IdPerfilProductos"
INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" det on det."IdPerfilProductos" = pp."IdPerfilProductos"
WHERE por."IdPortafolio" IN (:idsPortafolios);
