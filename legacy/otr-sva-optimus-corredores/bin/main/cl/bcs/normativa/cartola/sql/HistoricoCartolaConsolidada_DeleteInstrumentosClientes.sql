-- Limpia CartolaHistoricaInstrumentos para una nueva carga en la fecha indicada
DELETE 
FROM "BSCL.Normativa.BD::CAR.CartolaHistoricaInstrumentos"
WHERE "Fecha" = :p_Fecha;
