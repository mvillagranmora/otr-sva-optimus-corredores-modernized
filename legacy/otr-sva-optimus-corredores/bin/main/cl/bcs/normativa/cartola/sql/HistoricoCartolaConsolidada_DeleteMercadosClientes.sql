-- Limpia CartolaHistoricaMercados para una nueva carga en la fecha indicada
DELETE 
FROM "BSCL.Normativa.BD::CAR.CartolaHistoricaMercados"
WHERE "Fecha" = :p_Fecha;
