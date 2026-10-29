UPDATE "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico"
SET "EsUltimaCargaDiaria" = 'N'
WHERE 
"IdSerieFondo" = :IdSerieFondo
AND "FechaCierre" = :FechaVigencia
AND "IdEmpresa" = :IdEmpresa
AND "EsUltimaCargaDiaria" = 'S';