SELECT
"IdPlantillaLimite", 
"DescripcionPlantillaLimite"
FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarPlantillaLimites"
WHERE "IdPlantillaLimite" in (:IdsPlantillaLimite);