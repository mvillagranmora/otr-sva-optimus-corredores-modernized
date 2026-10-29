SELECT COUNT(1) AS "Valor5"
FROM "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" WHERE
"FechaPatrimonio" = (SELECT MAX("FechaPatrimonio") FROM "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja")
AND  "MontoPatrimonio" = 0