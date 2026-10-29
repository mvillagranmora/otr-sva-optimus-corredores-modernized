SELECT "IdPortafolio", "IdOperacion" 
FROM "BSCL.Mesa.BD::MES.Operacion" 
WHERE "IdPortafolio" IN (:IdsPortafolio);