SELECT
        "SerieDeFondo"."IdSerieFondo"
       ,"SerieDeFondo"."NombreSerieFondo"
       ,"Fondo"."IdMoneda"
       ,"Fondo"."IdEmisor"
       ,CASE 
          WHEN "RegimenFM"."RescateConMandato" = 'S' OR "RegimenFM"."AceptaMandatoEnReinversion" = 'S' THEN 'S' 
        ELSE 'N' 
        END AS "MLT"
       ,"Regimen"."IdRegimenFondoMutuo"
       ,IFNULL("ValorCuota",0) AS "ValorCuotaInstrumento"
--     ,IFNULL("PatrimonioMinimo",0) AS "PatrimonioMinimo" 
       ,IFNULL("SerieDeFondo"."MontoPatrimonioEmisor",0) AS "MontoPatrimonioEmisor"
       ,IFNULL("MontoMinimo",0) AS "MontoMinimo"
FROM "BSCL.FondoMutuo.BD::FMM.Fondo" AS "Fondo"
  INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS "SerieDeFondo" 
     ON ("SerieDeFondo"."IdFondo" = "Fondo"."IdFondo")
  LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS "SerieDeFondoVC" 
     ON "SerieDeFondoVC"."IdSerieFondo" = "SerieDeFondo"."IdSerieFondo"  
  LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS "Regimen" 
     ON "Regimen"."IdSerieFondo" = "SerieDeFondo"."IdSerieFondo" 
    AND "Regimen"."IdEstado"='ACT'
    AND "Regimen"."IdRegimenFondoMutuo" = (CASE
                                             WHEN :IdRegimenFondoMutuo <> '-' THEN :IdRegimenFondoMutuo
                                           ELSE "Regimen"."IdRegimenFondoMutuo"
                                           END)
  LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "RegimenFM"
    ON "RegimenFM"."IdRegimenFondoMutuo" = "Regimen"."IdRegimenFondoMutuo"
WHERE "SerieDeFondo"."IdEmpresa" = (CASE WHEN :IdEmpresa <> '' THEN :IdEmpresa ELSE "SerieDeFondo"."IdEmpresa" END) 
   AND "SerieDeFondo"."IdEstado" = (CASE WHEN :IdEstado <> '' THEN :IdEstado ELSE "SerieDeFondo"."IdEstado" END) 
   AND "Fondo"."IdMoneda" = (CASE WHEN :IdMoneda <> '-' THEN :IdMoneda ELSE "Fondo"."IdMoneda" END) 
   AND "Fondo"."FondoExtranjero" = (CASE WHEN :FondoExtranjero <> '-' THEN :FondoExtranjero ELSE "Fondo"."FondoExtranjero" END) 
   AND "SerieDeFondo"."IdFamiliaFondo" = (CASE WHEN :IdFamiliaFondo <> '-' THEN :IdFamiliaFondo ELSE "SerieDeFondo"."IdFamiliaFondo" END) 
   AND "Fondo"."IdEmisor" = (CASE WHEN :IdEmisor <> '-' THEN :IdEmisor ELSE "Fondo"."IdEmisor" END) 
   AND "SerieDeFondo"."IdSerieFondo" <> (CASE WHEN :IdSerieFondoOrigen <> '-' THEN :IdSerieFondoOrigen ELSE '-' END)
   AND COALESCE("Regimen"."IdRegimenFondoMutuo",'') = (CASE WHEN :IdRegimenFondoMutuo <> '-' THEN :IdRegimenFondoMutuo ELSE COALESCE("Regimen"."IdRegimenFondoMutuo",'') END)
