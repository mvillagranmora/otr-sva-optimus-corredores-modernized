-- Autorizar Persona: Establecer EsEnrolado = 'S' (asociada a Contraparte)
-- Migración de ConfirmaAutorizacionEnrolamientoContraparteMasivo.sql

UPDATE "BSCL.SocioNegocio.BD::PER.Persona" 
SET "EsEnrolado" = 'S',
    "IdEstado" = 'ACT'
WHERE "IdPersona" IN (
    SELECT DISTINCT CNT."IdPersona"
    FROM "BSCL.SocioNegocio.BD::CNT.Contraparte" CNT
    WHERE CNT."IdContraparte" = :IdContraparte
);
