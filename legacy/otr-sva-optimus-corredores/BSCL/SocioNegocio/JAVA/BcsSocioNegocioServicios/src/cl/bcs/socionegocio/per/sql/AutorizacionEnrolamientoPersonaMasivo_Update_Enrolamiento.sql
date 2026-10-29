UPDATE "BSCL.SocioNegocio.BD::PER.Persona"
SET "EsEnrolado" = :EsEnrolado
WHERE "IdPersona" = :IdPersona
  AND (
    (:EsEnrolado = 'S' AND "EsEnrolado" IN ('P', 'N'))  -- Confirmar: solo si está Pendiente o No enrolado
    OR 
    (:EsEnrolado = 'N' AND "EsEnrolado" = 'P')          -- Rechazar: solo si está Pendiente
  )
