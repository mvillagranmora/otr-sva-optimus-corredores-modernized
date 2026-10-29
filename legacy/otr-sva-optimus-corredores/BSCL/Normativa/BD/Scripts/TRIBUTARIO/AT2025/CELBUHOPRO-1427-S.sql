SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Titulo" = SUBSTRING(RTRIM("Titulo") || ' ' || 'Y/O IMPTO SUSTVO DE IMPTOS FINALES', 1, 100)
WHERE "Esquema" = 'DJ1922' AND "Version" = '2024' AND "CodigoSII" = 'C70';

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET "Titulo" = SUBSTRING(RTRIM("Titulo") || ' ' || 'y/o impto sustvo de imptos finales', 1, 100)
WHERE "Esquema" = 'DJ1949' AND "Version" = '2024' AND "CodigoSII" = 'C14';

COMMIT;