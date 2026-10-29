UPDATE "BSCL.SocioNegocio.BD::PRO.Proveedor"
SET "EsEnrolado" = :EsEnrolado
WHERE "IdProveedor" = :IdProveedor
  AND (
    (:EsEnrolado = 'S' AND "EsEnrolado" IN ('P', 'N'))  -- Confirmar: solo si está Pendiente o No enrolado
    OR 
    (:EsEnrolado = 'N' AND "EsEnrolado" = 'P')          -- Rechazar: solo si está Pendiente
  )
