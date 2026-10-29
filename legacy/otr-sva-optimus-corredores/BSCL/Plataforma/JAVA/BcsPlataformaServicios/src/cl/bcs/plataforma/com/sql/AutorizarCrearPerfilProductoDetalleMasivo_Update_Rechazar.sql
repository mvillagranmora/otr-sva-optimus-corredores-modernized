UPDATE "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
SET "IdEstadoAutorizacion" = 'RZD',
    "IdEstado" = 'INA'
WHERE "IdDetallePerfilProductos" = :IdDetallePerfilProductos
