SELECT 
"IdPersona",
"IdPaisResidencia",
"TIN",
"IdCausaNoTin",
"Observacion",
"PaisResidenciaPrincipal"
FROM "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia"
WHERE "IdPersona" IN (:IdPersona) AND "PaisResidenciaPrincipal" = 'S';