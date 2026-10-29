UPDATE TEL
SET "IdEstadoAutorizacion" = :p_IdEstadoAutorizacion
FROM "BSCL.SocioNegocio.BD::PER.Telefono" TEL
WHERE TEL."IdTelefono" = :p_IdTelefono;
