SELECT
  TO_VARCHAR(S."GENERATED_PERIOD_START", 'YYYY-MM-DD') AS "FechaHabilAnterior"
FROM SERIES_GENERATE_DATE(
        'INTERVAL 1 DAY',
        ADD_DAYS(TO_DATE(:fechaSistema, 'YYYY-MM-DD'), -31),
        TO_DATE(:fechaSistema, 'YYYY-MM-DD')
     ) S
WHERE S."GENERATED_PERIOD_START" < TO_DATE(:fechaSistema, 'YYYY-MM-DD')
  AND WEEKDAY(S."GENERATED_PERIOD_START") NOT IN (5, 6)
  AND NOT EXISTS (
      SELECT 1
      FROM "BSCL.Plataforma.BD::REG.DiaFeriado" F
      WHERE F."IdPais" = '44'
        AND F."Fecha" = S."GENERATED_PERIOD_START"
        AND F."AgnoCarga" IN (:anio, :anioPrev)
  )
ORDER BY S."GENERATED_PERIOD_START" DESC
LIMIT 1;
