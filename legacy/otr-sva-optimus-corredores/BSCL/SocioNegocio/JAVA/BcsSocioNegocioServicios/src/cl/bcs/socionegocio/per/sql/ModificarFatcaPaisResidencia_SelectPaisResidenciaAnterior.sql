SELECT "TIN", "IdCausaNoTin", "Observacion","PaisResidenciaPrincipal"
FROM "BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia"
WHERE "IdPersona"  = :IdPersona AND "IdPaisResidencia" = :IdPaisResidencia;