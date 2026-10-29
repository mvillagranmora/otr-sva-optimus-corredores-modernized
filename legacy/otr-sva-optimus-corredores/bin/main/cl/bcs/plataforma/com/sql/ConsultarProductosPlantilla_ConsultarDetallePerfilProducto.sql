SELECT pp."IdPerfilProductos",pp."IdEstado" AS "IdEstadoPerfilProducto",det."IdEstado" AS "IdEstadoDetallePerfilProducto",
		det."IdTipoProducto",
		CASE det."IdTipoProducto" WHEN '0' THEN 'TODOS' ELSE tipopro."Nombre" END AS "NombreTipoProducto",
		
		det."IdTipoOperacion",
		CASE det."IdTipoOperacion" WHEN '0' THEN 'TODOS' ELSE tipoope."Nombre" END AS "NombreTipoOperacion",
		
		det."IdCanalIngreso" as "IdTipoCanalIngreso",
		CASE det."IdCanalIngreso" WHEN '0' THEN 'TODOS' ELSE tipocanal."Nombre" END AS "NombreTipoCanalIngreso",
		
		pp."EsPerfilPersonalizado"
FROM "BSCL.Plataforma.BD::COM.PerfilProductos" pp 
INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" det on det."IdPerfilProductos" = pp."IdPerfilProductos"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tipoope ON tipoope."IdTipoOperacion" = det."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" tipopro ON tipopro."IdTipoProducto" = det."IdTipoProducto"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" tipocanal ON tipocanal."IdTipoCanalIngreso" = det."IdCanalIngreso"
WHERE pp."IdPerfilProductos" IN (:idsPerfilProductos);