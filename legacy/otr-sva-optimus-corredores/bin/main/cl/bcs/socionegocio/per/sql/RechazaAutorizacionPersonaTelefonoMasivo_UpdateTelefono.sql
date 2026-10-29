UPDATE TEL
SET "IdEstadoAutorizacion" = :p_IdEstadoAutorizacion,
    "IdEstado" = CASE WHEN :p_IdEstado IS NOT NULL THEN :p_IdEstado ELSE "IdEstado" END,
    "Borrado" = CASE WHEN :p_Borrado IS NOT NULL THEN :p_Borrado ELSE "Borrado" END
FROM "BSCL.SocioNegocio.BD::PER.Telefono" TEL
WHERE TEL."IdTelefono" = :p_IdTelefono;
