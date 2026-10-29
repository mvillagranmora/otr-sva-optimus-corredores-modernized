SELECT "IdPortafolio", "IdIngresoEgreso" 
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" 
WHERE "IdPortafolio" IN (:IdsPortafolio);