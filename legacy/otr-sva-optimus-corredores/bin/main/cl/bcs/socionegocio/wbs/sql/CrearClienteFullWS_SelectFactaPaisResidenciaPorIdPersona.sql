SELECT 
	FPR ."IdPaisResidencia" AS "IdPais",
	FPR ."TIN"              AS "Tin",
	FPR ."IdCausaNoTin"     AS "NoTin",
	FPR ."Observacion"      AS "OtraCausa"
FROM  "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia" AS FPR 
WHERE FPR ."IdPersona" IN (:IdPersona);