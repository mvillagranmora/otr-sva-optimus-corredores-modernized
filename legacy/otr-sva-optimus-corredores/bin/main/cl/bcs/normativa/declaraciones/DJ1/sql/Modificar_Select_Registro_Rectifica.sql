SELECT
"KIdContrato" AS "IdContrato"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" AS DDJ
WHERE "KIdContrato" IN (:KIdContrato) AND "Rectifica"='S'
AND "Seccion"=1;