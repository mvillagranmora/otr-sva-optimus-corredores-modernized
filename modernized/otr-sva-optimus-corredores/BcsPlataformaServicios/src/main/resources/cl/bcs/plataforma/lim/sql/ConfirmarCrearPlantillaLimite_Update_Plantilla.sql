UPDATE "BSCL.Plataforma.BD::LIM.PlantillaLimite"
SET "IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdPlantillaLimite" = :IdPlantillaLimite
	AND "IdEmpresa" = :IdEmpresa
