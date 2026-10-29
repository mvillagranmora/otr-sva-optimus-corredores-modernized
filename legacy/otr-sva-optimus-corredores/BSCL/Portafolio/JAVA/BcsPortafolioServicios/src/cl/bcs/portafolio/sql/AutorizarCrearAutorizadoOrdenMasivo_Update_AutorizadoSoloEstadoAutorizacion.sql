UPDATE "BSCL.Portafolio.BD::POR.Autorizado"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdAutorizado" IN (:IdAutorizado)
