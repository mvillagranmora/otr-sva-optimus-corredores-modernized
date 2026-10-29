UPDATE "BSCL.Portafolio.BD::POR.Autorizado"
SET "IdEstado" = :IdEstado,
    "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdAutorizado" = :IdAutorizado
