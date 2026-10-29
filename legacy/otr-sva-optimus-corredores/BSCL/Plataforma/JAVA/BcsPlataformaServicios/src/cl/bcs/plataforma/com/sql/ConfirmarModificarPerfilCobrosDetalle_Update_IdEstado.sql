UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
SET "IdEstado" = :ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
