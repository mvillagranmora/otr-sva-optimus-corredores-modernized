SELECT
"KIdContrato" AS "IdContrato",
"S2_Instrumento"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1" AS DDJ
WHERE "KIdContrato" IN (:KIdContrato)
AND "Seccion"=1;