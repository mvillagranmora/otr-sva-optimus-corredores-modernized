SELECT '' as "DescripcionPerfilComercialBase", * 
FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" 
WHERE "IdPerfilProductos" = '' 
AND "IdEmpresa" = :IdEmpresa
