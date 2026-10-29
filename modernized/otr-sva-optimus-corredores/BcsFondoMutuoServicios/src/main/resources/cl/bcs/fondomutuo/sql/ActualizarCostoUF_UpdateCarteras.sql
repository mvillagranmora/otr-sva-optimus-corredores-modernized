UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "CostoInversionEnFactorAjuste" = :CostoInversionEnFactorAjuste,
"EGRCostoInversionEnFactorAjuste" = :EGRCostoInversionEnFactorAjuste
WHERE "IdCartera" = :IdCartera