UPDATE "BSCL.Portafolio.BD::POR.Autorizado"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion,
    "IdEstado" = CASE WHEN :IdConcepto = 'NAUTOR' THEN 'INA' ELSE "IdEstado" END
WHERE "IdAutorizado" = :IdAutorizado