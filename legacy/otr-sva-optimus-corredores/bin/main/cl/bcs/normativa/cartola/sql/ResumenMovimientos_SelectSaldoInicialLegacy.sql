SELECT CAR."IdPortafolio" as "IdPortafolio",CAR."IdMoneda" as "IdMoneda",CAR."SaldoInicial" as "SaldoInicial"
FROM "BSCL.Normativa.BD::CAR.SaldoInicialSebra" CAR
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON CAR."IdPortafolio" = P."IdPortafolio"
WHERE "IdMoneda" IN (:IdMoneda)
AND COD_PER = TO_NVARCHAR(TO_DATE(:FechaDesde,'YYYY-MM-DD'),'YYYY-MM')