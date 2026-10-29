UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "Titulo"='Numero Certificado'
WHERE "Esquema"='DJ1949' AND "Version" IN ('2020','2021','2022')
AND "Nombre"='NumCertificado_C34' AND "Orden" = 35;

