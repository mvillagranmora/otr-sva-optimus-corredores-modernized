UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
SET "IdTipoOperacion" = :ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
