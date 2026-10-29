SELECT "IdEmpleadoEjecutivo"
FROM "BSCL.Portafolio.BD::POR.Portafolio"
WHERE "IdPortafolio" IN (:IdPortafolio)
GROUP BY "IdEmpleadoEjecutivo";