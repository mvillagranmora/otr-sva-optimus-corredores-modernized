SELECT 
"IdEnrolamiento"
,"IdPaisResidencia"
,"TIN"
,"IdCausaNoTin"
,"Observacion"
,"PaisResidenciaPrincipal"
FROM "BSCL.SocioNegocio.BD::ENR.FatcaPaisResidencia"
WHERE "IdEnrolamiento" IN (:IdEnrolamiento);
