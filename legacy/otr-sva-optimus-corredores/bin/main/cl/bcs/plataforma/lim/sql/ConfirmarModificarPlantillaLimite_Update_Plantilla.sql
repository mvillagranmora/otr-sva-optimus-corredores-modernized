UPDATE "BSCL.Plataforma.BD::LIM.PlantillaLimite"
SET 
	"DescripcionPlantillaLimite" = COALESCE(:DescripcionPlantillaLimite, "DescripcionPlantillaLimite"),
	"IdEstado" = COALESCE(:IdEstado, "IdEstado"),
	"IdEstadoAutorizacion" = :IdEstadoAutorizacion
WHERE "IdPlantillaLimite" = :IdPlantillaLimite
	AND "IdEmpresa" = :IdEmpresa
