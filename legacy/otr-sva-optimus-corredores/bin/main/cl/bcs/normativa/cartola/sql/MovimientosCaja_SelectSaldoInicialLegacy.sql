SELECT B."IdPortafolio" as "IdPortafolio","IdMoneda" as "IdMoneda","SaldoInicial" as "SaldoInicial"
FROM "BSCL.Normativa.BD::CAR.SaldoInicialSebra" B
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" as P on P."IdPortafolio" = B."IdPortafolio"
WHERE
"IdMoneda" IN (:IdMoneda)
AND COD_PER = TO_NVARCHAR(TO_DATE(:FechaDesde,'YYYY-MM-DD'),'YYYY-MM')