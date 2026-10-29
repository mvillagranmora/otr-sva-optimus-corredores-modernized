-- Rechazar Contraparte: Establecer EsEnrolado = 'N'
-- Migración de RechazaAutorizacionEnrolamientoContraparteMasivo.sql

UPDATE CNT
SET "EsEnrolado" = 'N'
FROM "BSCL.SocioNegocio.BD::CNT.Contraparte" CNT
WHERE CNT."IdContraparte" = :IdContraparte
AND CNT."EsEnrolado" = 'P';
