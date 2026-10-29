DELETE FROM "BSCL.FondoMutuo.BD::FMM.ProcesarReinversionRescate_BloqueoReinversion"
    WHERE "IdBloqueaReinversion" IN (:IdBloqueaReinversion);

