SELECT
"RegimenSerie"."IdSerieFondo" || '_' || "RegimenSerie"."IdRegimenFondoMutuo" AS "IdRegimenFondoMutuoSerieFondo"
,"RegimenSerie"."IdRegimenFondoMutuo"
,"RegimenSerie"."IdSerieFondo"
,"Regimen"."GrupoRegimen"
,"Regimen"."PropositoSerieRelacionado"
,"Regimen"."PermiteOtrosGrupos"
,"Regimen"."ValidaPortafolioCliente"
,"Regimen"."ValidaPortafolioAGF"
,"Regimen"."PermiteRescate"
,"Regimen"."TipoCarteraNormativa"
,"Regimen"."PermiteCambioGrupo"
,"Regimen"."RescateConMandato"
,"Regimen"."AceptaMandatoEnReinversion"
,"Regimen"."PermiteAporte"
,"Regimen"."PermiteTraspaso"
,"Regimen"."ValidaMontoMinimoInstrumento"
,"Regimen"."CertificaSinAsociado"
,CASE
    WHEN FON."FondoExtranjero" = 'S' THEN 'N'
    WHEN FON."FondoExtranjero" = 'N' THEN "Regimen"."RescateConMandato"
END AS "PermiteMLT"
FROM "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" AS "RegimenSerie"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS "Regimen" ON ("Regimen"."IdRegimenFondoMutuo" = "RegimenSerie"."IdRegimenFondoMutuo" AND "Regimen"."IdEstado" = 'ACT')
INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" "SER" ON "RegimenSerie"."IdSerieFondo"  = "SER"."IdSerieFondo"
INNER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" FON ON "SER"."IdFondo" = "FON"."IdFondo"
WHERE "RegimenSerie"."IdRegimenFondoMutuo" IN (:IdRegimenTributario) AND "RegimenSerie"."IdSerieFondo" IN (:IdSerieFondo) AND "RegimenSerie"."IdEmpresa" = :IdEmpresa AND "RegimenSerie"."IdEstado" = 'ACT';