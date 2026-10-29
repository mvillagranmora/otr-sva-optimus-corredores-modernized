UPDATE "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = IFNULL(:IdEstado, "IdEstado"),
    "Borrado" = IFNULL(:Borrado, "Borrado")
WHERE "IdTarjetaBancaria" = :IdTarjetaBancaria
