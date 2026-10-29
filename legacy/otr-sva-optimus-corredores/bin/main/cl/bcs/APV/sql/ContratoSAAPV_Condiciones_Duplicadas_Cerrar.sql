UPDATE "BSCL.APV.BD::SCS.ContratoSAAPV" CON
SET  CON."IdEstado" = 'CER'
WHERE  CON."IdContratoSAAPV" = :IdContratoSAAPV;