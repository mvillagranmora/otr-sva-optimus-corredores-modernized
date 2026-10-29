UPDATE "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = COALESCE(:IdEstado, "IdEstado")
WHERE "IdDeclaracionOrigenFondos" = :IdDeclaracionOrigenFondos
