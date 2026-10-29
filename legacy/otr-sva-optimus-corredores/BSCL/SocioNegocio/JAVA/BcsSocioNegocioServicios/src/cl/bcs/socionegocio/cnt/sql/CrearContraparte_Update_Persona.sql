-- Actualizar estado Persona: cuando no se requiere autorizacion de enrolamiento

UPDATE "BSCL.SocioNegocio.BD::PER.Persona" PER
SET 
    PER."IdEstado" = 'ACT',
    PER."EsEnrolado" = 'S'
WHERE EXISTS (
    SELECT 1
    FROM "BSCL.SocioNegocio.BD::CNT.Contraparte" CNT
    WHERE CNT."IdPersona" = PER."IdPersona"
      AND CNT."IdContraparte" = :IdContraparte
);

