UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
SET "DiasPlazo" = :ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
