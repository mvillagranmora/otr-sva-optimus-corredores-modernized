UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
SET "IdTipoProducto" = :ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
