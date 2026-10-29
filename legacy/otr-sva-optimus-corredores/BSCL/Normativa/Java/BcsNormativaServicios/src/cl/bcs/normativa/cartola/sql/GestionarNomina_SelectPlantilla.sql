SELECT MAX("IdPlantilla") AS "IdPlantilla"
FROM "BSCL.Normativa.BD::CAR.Plantilla"
WHERE "VigenteDesde" <= CURRENT_DATE