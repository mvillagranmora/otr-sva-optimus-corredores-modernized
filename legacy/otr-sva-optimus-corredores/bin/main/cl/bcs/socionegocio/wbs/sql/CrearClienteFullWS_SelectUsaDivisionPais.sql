SELECT C."IdCiudad"
,P."UsaDivisionPais" 
FROM "BSCL.Plataforma.BD::REG.Pais" P
LEFT JOIN  "BSCL.Plataforma.BD::REG.Ciudad" C ON P."IdPais"=C."IdPais"
WHERE C."IdCiudad" IN (:IdCiudad)