UPDATE "BSCL.APV.BD::SCS.DistribucionInversion" DIS
SET  DIS."IdEstado" = 'CER'
WHERE  DIS."IdContratoSAAPV" = :IdContratoSAAPV;