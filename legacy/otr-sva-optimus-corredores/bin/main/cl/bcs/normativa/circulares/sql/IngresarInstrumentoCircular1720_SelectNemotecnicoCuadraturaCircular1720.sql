SELECT 
CUS."IdCuadratura"
FROM "BSCL.Normativa.BD::PRO.CuadraturaCustodiaCircular1720" AS CUS
WHERE CUS."Nemotecnico" = :Nemotecnico AND CUS."FechaCuadraturaCustodia" = :FechaCuadraturaCustodia;