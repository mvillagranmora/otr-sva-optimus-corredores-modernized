UPDATE "BSCL.SocioNegocio.BD::PER.Persona" PER
SET "EsEnrolado" = :EsEnrolado
WHERE PER."IdPersona" IN (
    SELECT PRO."IdPersona" 
    FROM "BSCL.SocioNegocio.BD::PRO.Proveedor" PRO
    WHERE PRO."IdProveedor" = :IdProveedor
)
  AND PER."EsEnrolado" IN ('P', 'N')
  AND :EsEnrolado = 'S'  -- Solo se ejecuta en confirmar
