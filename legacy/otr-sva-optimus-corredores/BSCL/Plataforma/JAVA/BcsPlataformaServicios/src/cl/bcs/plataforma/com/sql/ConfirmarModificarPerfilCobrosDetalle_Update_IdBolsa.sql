UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
SET "IdBolsa" = :ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
