SELECT COUNT(1) AS "Contador"
FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarPlantillaLimites" PL
WHERE PL."IdPlantillaLimite" = :ValorNuevo
AND PL."PorDefecto" = 'S'
