SELECT 	"IdBloqueaReinversion"
FROM "BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion"
WHERE "IdBloqueaReinversion" IN (:p_IdBloqueaReinversion);