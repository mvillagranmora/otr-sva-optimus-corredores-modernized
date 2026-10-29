SELECT det."IdPerfilCobros",det."IdDetallePerfilCobros",det."IdPerfilCobros",det."IdTipoProducto",det."IdTipoOperacion",det."IdBolsa",det."IdTipoCanalIngreso",det."CodigoDefinicionCobro"
FROM "BSCL.Plataforma.BD::COM_2.DetallePerfilCobro" det
LEFT JOIN "BSCL.Plataforma.BD::COM_2.PerfilCobros" per on det."IdPerfilCobros" = per."IdPerfilCobros"
WHERE det."IdDetallePerfilCobros" in (:Datos) AND det."IdEmpresa" = :IdEmpresa AND det."Borrado" = 'N'