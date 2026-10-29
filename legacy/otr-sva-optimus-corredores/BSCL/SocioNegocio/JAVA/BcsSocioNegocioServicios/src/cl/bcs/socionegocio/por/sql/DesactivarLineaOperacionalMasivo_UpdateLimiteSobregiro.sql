UPDATE "BSCL.Plataforma.BD::LIM.Limite" 
SET "IdEstado" = 'INA',  
	"MontoTotal" = 0
WHERE "IdTipoLimite" = 'LIMPORTSOBREGIRO' AND "UI_Limite" = 'LIMITE' AND "IdPlantillaLimite" NOT IN (SELECT 'EXT_' || "IdPortafolio" FROM "BSCL.Portafolio.BD::POR.Portafolio" WHERE "ExcluyeLimpiezaLineaOperacional" = 'S');