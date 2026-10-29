
SELECT "IdInstrumento", "Precio", "Tasa"
FROM "BSCL.Plataforma.BD::INS.Instrumento"
WHERE "IdInstrumento" IN (:idsInstrumento) 
  AND "Precio" IS NOT NULL; 
--  AND "Precio" > 0;
