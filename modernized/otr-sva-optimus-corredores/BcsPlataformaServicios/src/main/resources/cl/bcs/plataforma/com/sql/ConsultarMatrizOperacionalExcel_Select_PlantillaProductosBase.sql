SELECT P."DescripcionPerfilComercialBase", C.* 
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" P 
JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" C 
	ON C."IdPerfilProductos" = P."IdPerfilProductos"
WHERE P."IdEmpresa" = :IdEmpresa
