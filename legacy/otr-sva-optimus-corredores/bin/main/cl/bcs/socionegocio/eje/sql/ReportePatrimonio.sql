SELECT 
A1."IdCliente", 
A1."NombreCliente", 
A1."IdAgente",
A1."NombreAgente",
A2."IdMercadoCartola",
A2."NombreMercadoCartola",
A2."MontoTotalMercado"
FROM "BSCL.Normativa.BD::CAR.CartolaHistorica" AS A1 
INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistoricaMercados" AS A2 
	ON A1."IdCliente" = A2."IdCliente" AND (A1."Fecha" = A2."Fecha")
WHERE ("IdAgente" = :p_IdAgente OR :p_IdAgente = 0)
	AND (A1."Fecha" = :p_FechaConsulta )
ORDER BY A1."IdCliente";





