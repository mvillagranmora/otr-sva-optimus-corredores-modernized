UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
SET "Prioridad" = :ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
