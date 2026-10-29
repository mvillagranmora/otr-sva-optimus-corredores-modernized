SELECT DISTINCT A2."NombreMercadoCartola", "IdMercadoCartola"
FROM "BSCL.Normativa.BD::CAR.CartolaHistorica" AS A1 
INNER JOIN "BSCL.Normativa.BD::CAR.CartolaHistoricaMercados" AS A2  
ON A1."IdCliente" = A2."IdCliente" 
WHERE ("IdAgente" = :p_IdAgente OR COALESCE(:p_IdAgente, 0) = 0) and (A1."Fecha" = :p_FechaConsulta);