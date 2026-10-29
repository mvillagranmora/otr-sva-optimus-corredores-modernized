UPDATE "BSCL.Mesa.BD::SPT.CrearOperacionSpot_Proceso"
SET "Estado" = :Estado
WHERE "NumeroOperacion" IN (:NumeroOperacion);
