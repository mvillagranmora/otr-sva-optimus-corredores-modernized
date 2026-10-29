UPDATE  "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
SET "CostoUF"		  = CASE "IdRegimenTributario" WHEN '18Q' THEN :Valor ELSE "CostoUF" END,
    "RentabilidadReal"= CASE "IdRegimenTributario" WHEN '18Q' THEN "RentabilidadReal" ELSE :Valor END
WHERE "IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate);