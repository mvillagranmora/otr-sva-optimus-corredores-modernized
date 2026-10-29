-- Limpia CartolaHistorica para una nueva carga en la fecha indicada
DELETE 
FROM "BSCL.Normativa.BD::CAR.CartolaHistorica"
WHERE "Fecha" = :p_Fecha;