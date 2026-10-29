-- Autorizar Contraparte: Establecer EsEnrolado = 'S'
-- Migración de ConfirmaAutorizacionEnrolamientoContraparteMasivo.sql

UPDATE CNT
SET CNT."EsEnrolado" = 'S'
FROM "BSCL.SocioNegocio.BD::CNT.Contraparte" CNT
WHERE CNT."IdContraparte" = :IdContraparte
AND CNT."EsEnrolado" IN ('P','N');
