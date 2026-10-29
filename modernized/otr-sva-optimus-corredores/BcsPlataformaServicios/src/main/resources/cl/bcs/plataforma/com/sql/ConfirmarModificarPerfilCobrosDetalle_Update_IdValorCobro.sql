UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
SET "IdValorCobro" = :ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
