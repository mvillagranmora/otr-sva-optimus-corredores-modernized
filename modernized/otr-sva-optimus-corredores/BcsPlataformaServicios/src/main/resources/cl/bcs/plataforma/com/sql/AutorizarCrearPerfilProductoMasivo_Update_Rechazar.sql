UPDATE "BSCL.Plataforma.BD::COM.PerfilProductos"
SET "IdEstadoAutorizacion" = 'RZD',
    "IdEstado" = 'INA'
WHERE "IdPerfilProductos" = :IdPerfilProductos
