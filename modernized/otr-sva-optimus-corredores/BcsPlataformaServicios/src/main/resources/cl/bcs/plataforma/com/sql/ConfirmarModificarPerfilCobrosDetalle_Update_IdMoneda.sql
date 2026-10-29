UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
SET "IdMoneda" = :ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
