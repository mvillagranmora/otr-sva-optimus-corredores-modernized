SELECT "IdDetallePerfilCobros","IdPerfilCobros","IdTipoProducto","IdTipoOperacion","IdBolsa","IdTipoCanalIngreso","CodigoDefinicionCobro","CodigoDefinicionCobroBase"
FROM "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro"
WHERE "IdPerfilCobros" = :IdPerfilCobros AND "IdEmpresa" = :IdEmpresa AND "Borrado" = :Borrado
