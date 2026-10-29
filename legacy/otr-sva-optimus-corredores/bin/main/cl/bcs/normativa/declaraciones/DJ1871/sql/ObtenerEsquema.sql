SELECT
    "FechaDatosHasta",
    TO_CHAR(YEAR("FechaDatosHasta")||'-11','YYYY-MM') AS "AgnoMes"
FROM "BSCL.Normativa.BD::DOC.Esquema"
WHERE "Esquema" = 'DJ1871'
AND "Version" = :Version;