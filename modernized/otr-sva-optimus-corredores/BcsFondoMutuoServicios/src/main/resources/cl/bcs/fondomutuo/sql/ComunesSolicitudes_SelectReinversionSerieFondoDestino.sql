SELECT R."IdReinversionSerieFondo", R."IdSolicitudAporteRescate", R."IdSerieFondoDestino", R."Porcentaje", R."IdRegimenTributario", S."IdFamiliaFondo", F."IdMoneda", R."Switch"
FROM "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" R
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" S ON (S."IdSerieFondo" = R."IdSerieFondoDestino")
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" F ON (F."IdFondo" = S."IdFondo")
WHERE R."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate)