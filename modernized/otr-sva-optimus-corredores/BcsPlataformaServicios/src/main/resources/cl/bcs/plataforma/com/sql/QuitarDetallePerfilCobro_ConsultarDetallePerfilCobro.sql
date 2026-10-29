SELECT "IdDetallePerfilCobros","IdPerfilCobros","IdTipoProducto","IdTipoOperacion","IdBolsa","IdCanalIngreso"
FROM "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
WHERE "IdDetallePerfilCobros" in (:Datos) AND "Borrado" = 'N'