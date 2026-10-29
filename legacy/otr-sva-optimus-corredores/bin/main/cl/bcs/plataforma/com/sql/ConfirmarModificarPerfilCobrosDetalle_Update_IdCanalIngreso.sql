UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilCobros"
SET "IdCanalIngreso" = :ValorNuevo,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdDetallePerfilCobros" = :IdDetallePerfilCobros
