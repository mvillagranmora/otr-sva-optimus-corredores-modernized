SELECT 
"IdDivisionPais" AS "IdComuna"
,"IdCiudad" 
FROM "BSCL.Plataforma.BD::REG.DivisionPais"
WHERE "IdDivisionPais" IN (:IdComunas);