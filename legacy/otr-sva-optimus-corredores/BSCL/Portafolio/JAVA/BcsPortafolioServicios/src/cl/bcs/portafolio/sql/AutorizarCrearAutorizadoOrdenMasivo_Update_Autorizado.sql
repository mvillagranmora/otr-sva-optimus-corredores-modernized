UPDATE "BSCL.Portafolio.BD::POR.Autorizado"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = :IdEstado
WHERE "IdAutorizado" IN (:IdAutorizado)
