SELECT TOP 1 
    "Valor" AS "ValorDolar"
FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero"
WHERE "IdParametroFinanciero" = 'DO'
  AND "Fecha" <= :FechaOperacion
  AND "Valor" > 0  
ORDER BY "Fecha" DESC;